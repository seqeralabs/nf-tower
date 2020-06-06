/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.service

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Phaser
import java.util.concurrent.TimeUnit

import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.reactivex.BackpressureStrategy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowKey
import io.seqera.tower.exceptions.AbortTransactionException
import io.seqera.tower.exchange.trace.TraceBeginRequest
import io.seqera.tower.exchange.trace.TraceCompleteRequest
import io.seqera.tower.exchange.trace.TraceProgressData
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.service.audit.AuditEventPublisher
import io.seqera.tower.service.cost.CostService
import io.seqera.tower.service.progress.ProgressService
import io.seqera.tower.util.ThreadPoolBuilder
import org.springframework.transaction.annotation.Propagation
import org.springframework.validation.FieldError

@Slf4j
@Singleton
@Transactional
@CompileStatic
class TraceServiceImpl implements TraceService {

    private static final Random random = new Random()

    @EqualsAndHashCode
    @Canonical
    private static class TaskEntry {
        Task task
        Workflow workflow
    }

    @Value('${trace.tasks.buffer.count:10000}')
    Integer bufferSize

    @Value('${trace.tasks.thread-pool.min-size:10}')
    Integer poolMinSize

    @Value('${trace.tasks.thread-pool.max-size:100}')
    Integer poolMaxSize

    @Value('${trace.tasks.thread-pool.queue-size:1000}')
    Integer poolQueueSize

    @Value('${trace.tasks.thread-pool.termination-timeout:30s}')
    Duration poolTerminationTimeout

    @Value('${trace.tasks.maxRetries:3}')
    Integer maxAttempts

    @Inject WorkflowService workflowService
    @Inject CostService costService
    @Inject TaskService taskService
    @Inject ProgressService progressService
    @Inject AuditEventPublisher eventPublisher

    private Phaser phaser
    private ExecutorService taskSaveExecutor

    @PostConstruct
    void init() {
        log.info "+ Creating trace service poolMinSize=$poolMinSize; poolMaxSize=$poolMaxSize; poolQueueSize=$poolQueueSize; maxAttempts=$maxAttempts; aggregate bufferCount=$bufferSize; poolTerminationTimeout=$poolTerminationTimeout"
        phaser = new Phaser()
        phaser.register()
        // executor to save tasks
        taskSaveExecutor = ThreadPoolBuilder.io(poolMinSize,poolMaxSize,poolQueueSize,'trace-pool')
    }

    @PreDestroy
    void cleanup() {
        log.info "< Flushing trace tasks publisher"
        // the onComplete event is needed to flush partial buffered task entries
        phaser.arriveAndAwaitAdvance()
        // shutdown the thread pool
        taskSaveExecutor.shutdown()
        taskSaveExecutor.awaitTermination(poolTerminationTimeout.toMillis(), TimeUnit.MILLISECONDS)
    }

    @NotTransactional
    protected TaskEntry safeCheckTask(TaskEntry entry) {
        try {
            return checkTask(entry)
        }
        catch( Throwable e ) {
            log.error("Unable to determine compute cost for taskId=${entry.task.taskId}; workflow Id=$entry.workflow.id", e)
            return entry
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected TaskEntry checkTask(TaskEntry entry) {
        if( entry.task.status.terminal )
            entry.task.cost = costService.computeCost(entry.task)
        return entry
    }

    @NotTransactional
    protected void safeSaveTask(TaskEntry entry) {
        int attempt = 1
        while(true) try {
            saveTask(entry)
            return
        }
        catch( AbortTransactionException e ) {
            log.warn(e.message)
            break
        }
        catch( Exception e ) {
            def msg = "Failed to save task entry id=${entry?.task?.id}; taskId=${entry?.task?.taskId}; workflow id=${entry?.workflow?.id}; attempt=$attempt"
            if( attempt++ >= maxAttempts ) {
                log.error(msg, e)
                break
            }
            // sleep and try it again
            final delay=50  + random.nextInt(500)
            msg += "; cause=${e.message ?: e.toString()}; await $delay msg and retry"
            log.error(msg)
            sleep(delay)
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected Task saveTask(TaskEntry entry) {
        log.trace "Saving task entry=$entry"
        taskService.saveTask(entry.task, entry.workflow)
    }

    @NotTransactional
    protected safeAggregateMetrics(List<TaskEntry> entries) {
        int attempt = 1
        while(true) try {
            aggregateMetrics(entries)
            return
        }
        catch( Exception e ) {
            def msg = "Failed to aggregate metrics for task entries=${entries?.task?.id}; taskId=${entries?.task?.taskId}; workkflow id=${entries?.workflow?.id}; attempt=$attempt"
            if( attempt++ >= maxAttempts ) {
                log.error(msg, e)
                break
            }
            // sleep and try it again
            final delay = 50  + random.nextInt(500)
            msg += "; cause=${e.message ?: e.toString()}; await $delay msg and retry"
            log.error(msg)
            sleep(delay)
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected aggregateMetrics(List<TaskEntry> entries) {
        log.trace "Trace aggregating metrics workflowId=${entries.first()?.workflow?.id} size=${entries.size()}"
        def workflowId = entries.first().workflow.id
        def tasks = entries.collect { it.task }
        progressService.aggregateMetrics(workflowId, tasks)
    }


    Workflow processWorkflowTrace(TraceWorkflowRequest request, User owner) {
        final workflow = workflowService.processTraceWorkflowRequest(request, owner)
        if( workflow.checkIsRunning() ) {
            progressService.create(workflow.id, request.processNames)
            eventPublisher.workflowCreation(workflow.id)
        }
        else {
            eventPublisher.workflowCompletion(workflow.id)
        }
        checkWorkflowSaveErrors(workflow)

        return workflow
    }


    List<Task> processTaskTrace(TraceTaskRequest request) {
        List<Task> tasks = taskService.processTaskTraceRequest(request)

        for( Task task : tasks ) {
            checkTaskSaveErrors(task)
        }

        progressService.updateStats(request.workflowId, tasks)

        return tasks
    }

    private void checkWorkflowSaveErrors(Workflow workflow) {
        if (!workflow.hasErrors()) {
            return
        }

        List<FieldError> fieldErrors = workflow.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            throw new ValidationException("Can't save a workflow without ${nullableError.field}")
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            throw new ValidationException("Can't save a workflow with the same ${uniqueError.field} of another")
        }

        List<String> uncustomizedErrors = fieldErrors.collect { "${it.field}|${it.code}".toString() }
        throw new ValidationException("Can't save task. Validation errors: ${uncustomizedErrors}")
    }

    private void checkTaskSaveErrors(Task task) {
        if (!task.hasErrors()) {
            return
        }

        List<FieldError> fieldErrors = task.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            throw new ValidationException("Can't save a task without ${nullableError.field}")
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            throw new ValidationException("Can't save a task with the same ${uniqueError.field} of another")
        }

        List<String> uncustomizedErrors = fieldErrors.collect { "${it.field}|${it.code}".toString() }
        throw new ValidationException("Can't save task. Validation errors: ${uncustomizedErrors}")
    }

    @Deprecated
    @Override
    void keepAlive(String workflowId) {
        workflowService.markForRunning(workflowId)
        progressService.updateStats(workflowId, Collections.<Task>emptyList())
    }

    // --== new API ==--

    @Override
    Workflow handleFlowBegin(TraceBeginRequest request, User user) {
        if( !request.workflow.checkIsRunning() )
            throw new IllegalStateException("Workflow status should be running -- current=${request.workflow.status}")

        final workflow = workflowService.createWorkflow(request, user)

        progressService.updateProgress(workflow.id, TraceProgressData.EMPTY)
        eventPublisher.workflowCreation(workflow.id)
        return workflow
    }

    @Override
    Workflow handleFlowComplete(TraceCompleteRequest request, User user) {
        if( !request.workflow )
            throw new IllegalStateException("Missing completion request workflow")

        if( !request.workflow.id )
            throw new IllegalStateException("Missing completion request workflow id")

        if( !request.workflow.checkIsComplete() )
            throw new IllegalStateException("Workflow status should be complete -- current=${request.workflow.status}")

        // update live progress
        if( request.progress ) {
            log.debug "> Complete update progress"
            progressService.updateProgress(request.workflow.id, request.progress)
        }

        final workflow = workflowService.updateWorkflow(request.workflow, request.metrics)
        checkWorkflowSaveErrors(workflow)
        eventPublisher.workflowCompletion(workflow.id)
        return workflow
    }

    /**
     * This does thee main things:
     * 1) store the workflow current progress computed by NF
     *    in the progress store so that it can be delivered to
     *    the front-end to show the current execution progress
     *
     * 2) save the received tasks into the DB
     *
     * 3) aggregate the execution metrics for completed tasks
     *
     * @param req
     */
    void handleTaskTrace(String workflowId, TraceProgressData progress, List<Task> tasks) {
        assert workflowId, 'Workflow ID cannot be empty in trace request'
        assert progress != null, 'Workflow progress cannot be null in trace request'
        assert tasks != null, 'Workflow tasks cannot be null in trace request'

        // update live progress
        progressService.updateProgress(workflowId, progress)

        // save & aggregate metrics
        final workflow = Workflow.get(workflowId)
        if( !workflow )
            throw new IllegalArgumentException("Invalid trace workflow id: $workflowId")
        if( !tasks )
            return

        // save tasks using a rx publisher in separate thread
        final publisher = createTasksPublisher()
        for( Task task : tasks )
            publisher.onNext(new TaskEntry(task, workflow))
        publisher.onComplete()
    }

    @Override
    void handleHeartbeat(String workflowId, TraceProgressData progress) {
        workflowService.markForRunning(workflowId)
        progressService.updateProgress(workflowId, progress)
    }

    protected PublishSubject<TaskEntry> createTasksPublisher() {
        phaser.bulkRegister(2)

        PublishSubject<TaskEntry> subject = PublishSubject.create()

        def receiver = subject
                .toFlowable(BackpressureStrategy.BUFFER)
                .parallel()
                .runOn( Schedulers.from(taskSaveExecutor) )
                .map { TaskEntry task -> safeCheckTask(task) }
                .sequential()
                .share()

        // save tasks
        receiver
                .doOnComplete { phaser.arriveAndDeregister() }
                .subscribe( { task -> safeSaveTask(task) }, { err-> log.error("Unexpected error while saving task",err) })

        // aggregate metrics
        receiver
                .buffer(bufferSize)
                .flatMapIterable { List<TaskEntry> entries -> entries.groupBy { it.workflow.id }.values() }
                .doOnComplete { phaser.arriveAndDeregister() }
                .subscribe({ safeAggregateMetrics(it) }, { err-> log.error("Unexpected error while aggregating metrics",err) })

        return subject
    }
}

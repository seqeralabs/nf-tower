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
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException
import java.time.Duration
import java.util.concurrent.TimeUnit

import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import groovy.transform.Canonical
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.seqera.tower.domain.HashSequenceGenerator
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowKey
import io.seqera.tower.exchange.trace.TraceBeginRequest
import io.seqera.tower.exchange.trace.TraceCompleteRequest
import io.seqera.tower.exchange.trace.TraceProgressData
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.service.audit.AuditEventPublisher
import io.seqera.tower.service.cost.CostService
import io.seqera.tower.service.progress.ProgressService
import org.springframework.validation.FieldError

@Slf4j
@Singleton
@Transactional
class TraceServiceImpl implements TraceService {

    @Canonical
    private static class TaskEntry {
        Task task
        Workflow workflow
    }

    @Value('${trace.tasks.buffer.time:5s}')
    Duration bufferTimeout

    @Value('${trace.tasks.buffer.count:10000}')
    Integer bufferCount

    @Inject WorkflowService workflowService
    @Inject CostService costService
    @Inject TaskService taskService
    @Inject TransactionService transactionService
    @Inject ProgressService progressService
    @Inject AuditEventPublisher eventPublisher

    PublishSubject<TaskEntry> taskProcessor

    @PostConstruct
    void init() {
        log.debug "+ Creating trace tasks publisher buffer-count=$bufferCount; buffer-time=$bufferTimeout"
        taskProcessor = createTaskPublisher()
    }

    @EventListener
    void cleanup(ShutdownEvent event) {
        log.debug "+ Flushing trace tasks publisher"
        // the onComplete event is needed to flush partial buffered task entries
        taskProcessor.onComplete()
    }

    protected PublishSubject<TaskEntry> createTaskPublisher() {

        taskProcessor = PublishSubject.<TaskEntry>create()

        // check everything is fine
        Observable<TaskEntry> receiver = taskProcessor
                .map { TaskEntry task -> checkTask(task) }

        // save the tasks
        receiver
                .observeOn(Schedulers.io())
                .subscribe { task -> saveTask(task) }

        // aggregate metrics
        receiver
                .observeOn(Schedulers.computation())
                .buffer(bufferTimeout.toMillis(), TimeUnit.MILLISECONDS, bufferCount)
                .flatMapIterable { List<TaskEntry> entries -> entries.groupBy { it.workflow.id }.values() }
                .subscribe({ aggregateMetrics(it) }, { err-> log.error("Unexpected task receiver error",err) })

        return taskProcessor
    }

    protected TaskEntry checkTask(TaskEntry entry) {
        if( entry.task.status.terminated )
            entry.task.cost = costService.computeCost(entry.task)
        return entry
    }

    protected Task saveTask(TaskEntry entry) {
        taskService.saveTask(entry.task, entry.workflow)
    }

    protected aggregateMetrics(List<TaskEntry> entries) {
        log.trace "Trace aggregating metrics workflowId=${entries.first()?.workflow?.id} size=${entries.size()}"
        def workflowId = entries.first().workflow.id
        def tasks = entries.collect { it.task }
        progressService.aggregateMetrics(workflowId, tasks)
    }


    @NotTransactional
    String createWorkflowKey() {
        final record = transactionService.withTransaction { new WorkflowKey() .save() }
        final workflowId = HashSequenceGenerator.getHash(record.id)
        transactionService.withTransaction { record.workflowId=workflowId; record.save() }
        return workflowId
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

        final workflow = workflowService.createWorkflow(request.workflow, request.processNames, user)
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
        Workflow workflow = Workflow.get(workflowId)
        for( Task task : tasks )
            taskProcessor.onNext(new TaskEntry(task, workflow))
    }

    @Override
    void handleHeartbeat(String workflowId, TraceProgressData progress) {
        workflowService.markForRunning(workflowId)
        progressService.updateProgress(workflowId, progress)
    }
}

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

package io.seqera.tower.service.progress

import static io.seqera.tower.enums.WorkflowStatus.*

import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Value
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.seqera.tower.domain.ProcessLoad
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.service.LiveEventsService
import org.hibernate.Session
import org.springframework.transaction.annotation.Propagation
/**
 * Implements the workflow execution progress logic
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@Singleton
class ProgressServiceImpl implements ProgressService {

    @Inject
    ProgressStore store

    @Value('${tower.metrics.interval:`1m`}')
    Duration metrics

    @Value('${tower.trace.timeout:`400s`}')
    Duration aliveTimeout

    @Inject
    LiveEventsService liveEventsService

    @Inject
    ProgressOperationsImpl target

    PublishSubject publisher

    @PostConstruct
    void init() {
        log.info "Creating execution progress watcher -- store=${store.getClass().getSimpleName()}; metrics-interval=${metrics}; trace-timeout=${aliveTimeout}"
        publisher = PublishSubject.create()

        publisher
                .mergeWith( Observable.interval(metrics.toMillis(), TimeUnit.MILLISECONDS) )
                .observeOn(Schedulers.io())
                .subscribe { doEvent(it) }
    }

    List<ProgressState> getStats() {
        target.getStats()
    }

    @Override
    List<String> findExpired(Duration duration) {
        return target.findExpired(duration)
    }

    ProgressData getProgressData(String workflowId) {
        return target.getProgressData(workflowId)
    }

    void create(String workflowId, List<String> processNames) {
        // defer the invocation
        publisher.onNext( { target.create(workflowId, processNames) } )
    }

    void updateStats(String workflowId, List<Task> tasks) {
        // defer the invocation
        publisher.onNext( { target.updateStats(workflowId, tasks) } )
    }

    void complete(String workflowId) {
        // defer the invocation
        publisher.onNext( { target.complete(workflowId) } )
    }

    void doEvent(event) {
        if( event instanceof Closure ) {
            event.call()
        }
        else {
            doPeriodicLoadCheck()
        }
    }

    /*
     * clean up logic
     */

    protected void doPeriodicLoadCheck() {
        log.trace "Running periodic workflow load house cleaning"
        // delete stalled workflow execution
        // and delete if need
        final ids = findExpired(aliveTimeout)
        if( ids ) {
            killZombies(ids)
        }
    }


    protected void killZombies(List<String> zombies) {
        log.info "Unknown execution status for workflow=$zombies"
        for( String workflowId : zombies ) {
            markWorkflowUnknownStatus(workflowId)
        }
    }

    protected void markWorkflowUnknownStatus(String workflowId) {
        try {
            final workflow = markWorkflowUnknownStatus0(workflowId)
            if( workflow ) {
                // notify the status change
                liveEventsService.publishWorkflowEvent(workflow)
            }
            // remove progress from the cache in any case to avoid entering an endless re-try
            store.deleteProgress(workflowId)
        }
        catch( Exception e ) {
            log.error("Unable to save workflow with Id=$workflowId", e)
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected Workflow markWorkflowUnknownStatus0(String workflowId) {
        final workflow = Workflow.get(workflowId)
        if( !workflow ) {
            log.warn "Unknown workflow for Id=$workflowId | Ignore timeout marking event"
            return null
        }

        if( workflow.status==null || workflow.status==RUNNING ) {
            log.debug "Marking workflow Id=$workflowId with unknown status"
            workflow.status = UNKNOWN
            workflow.duration = computeDuration(workflow.start)
            workflow.save()
            // save process state
            target.persistProgressData(workflowId)
            return workflow
        }
        else {
            log.warn "Invalid status for workflow Id=$workflowId | Expected status=RUNNIG; found=$workflow.status"
            return null
        }
    }

    protected Long computeDuration(OffsetDateTime ts) {
        if( ts==null ) return null
        final result = Instant.now().toEpochMilli()-ts.toInstant().toEpochMilli()
        result>=0 ? result : 0
    }


    /*
     * deprecated
     */

    @Deprecated
    ProgressData getProgressQuery(Workflow workflow) {
        assert workflow?.id

        final progress = computeWorkflowProgress(workflow.id)
        // return the peak values stored in the workflow entity
        progress.workflowProgress.peakCpus = workflow.peakLoadCpus ?: 0
        progress.workflowProgress.peakTasks = workflow.peakLoadTasks ?: 0
        progress.workflowProgress.peakMemory = workflow.peakLoadMemory ?: 0

        return progress
    }

    @Deprecated
    @Transactional(readOnly = true)
    @CompileDynamic
    ProgressData computeWorkflowProgress(String workflowId) {
        def sql = """\
            select
               p.name,
               t.status,
               count(*),
               sum(t.cpus) as totalCpus,
               sum(t.cpus * t.realtime) as cpuTime,
               sum(t.pcpu * t.realtime / 100) as cpuLoad,
               sum(t.peak_rss) as memoryRss,
               sum(t.memory) as memoryReq,
               sum(t.rchar) as diskReads,
               sum(t.wchar) as diskWrites,
               sum(t.vol_Ctxt) as volCtxt,
               sum(t.inv_Ctxt) as invCtxt

             from tw_workflow_process p
               left join (
                    select
                       x.id, x.status, x.workflow_id, 
                       y.process, y.cpus, y.pcpu, y.realtime, y.peak_rss, y.memory, y.rchar, y.wchar, y.vol_ctxt, y.inv_ctxt 
                    from 
                       tw_task x, tw_task_data y 
                    where x.data_id=y.id and x.workflow_id = '$workflowId') t 
                 on 
                    p.workflow_id = t.workflow_id and p.name = t.process
             where
                p.workflow_id = '$workflowId'
             group by 
                p.name, t.status, p.position
             order by 
                p.position """.stripIndent()

        def tasks = Task.withSession { Session session -> session.createNativeQuery(sql).list() }

        // aggregate tasks by name and status
        final workflowProgress = new WorkflowLoad()
        final aggregate = new LinkedHashMap<String, ProcessLoad>(20)
        for( List cols : tasks ) {
            final row = new ProgressRow(cols)
            // aggregate by process name
            def name = row.process
            def process = aggregate.get(name)
            if( process == null ) {
                process = new ProcessLoad(process: name)
                aggregate.put(name, process)
            }
            process.plus(row)

            // aggregate all nums for workflow
            workflowProgress.plus(row)
        }

        // aggregate workflow process
        final processProgresses = new ArrayList<ProcessLoad>(aggregate.values()).sort{it.process}
        new ProgressData(workflowProgress: workflowProgress, processesProgress: processProgresses)
    }


}

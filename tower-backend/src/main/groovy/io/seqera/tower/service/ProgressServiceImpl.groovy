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
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.reactivex.Observable
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.enums.WorkflowStatus
import io.seqera.tower.exchange.progress.ProcessProgress
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.progress.WorkflowProgress
import org.hibernate.Session
import org.springframework.transaction.annotation.Propagation

/**
 * Implements the workflow execution progress & monitoring logic
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class ProgressServiceImpl implements ProgressService {

    Map<String, LoadStats> loadStats = new ConcurrentHashMap<>()

    @Value('${tower.metrics.interval:`1m`}')
    Duration metrics

    @Value('${tower.trace.timeout:`190s`}')
    Duration aliveTimeout

    @Inject
    LiveEventsService liveEventsService

    @PostConstruct
    void init() {
        log.info "Creating execution load watcher metrics-interval=${metrics}; trace-timeout=${aliveTimeout}"
        Observable
                .interval(metrics.toMillis(), TimeUnit.MILLISECONDS)
                .subscribe { doPeriodicLoadCheck() }
    }

    @Override
    Map<String, LoadStats> getLoadStats() {
        new HashMap<String, LoadStats>(loadStats)
    }

    @Override
    void progressCreate(String workflowId) {
        doEventCreate(workflowId)
    }

    @Override
    void progressUpdate(String workflowId, List<Task> tasks) {
        final load = new HashMap<Long,TaskLoad>(tasks.size())
        final terminated = new ArrayList<Long>(tasks.size())
        for( Task task : tasks ) {
            if( task.status == TaskStatus.RUNNING ) {
                load[task.id] = new TaskLoad(cpus: task.cpus?:0, memory: task.memory?:0)
            }
            else if( task.status?.isTerminated() ) {
                terminated.add(task.id)
            }
        }

        doEventUpdate(workflowId, load, terminated)
    }


    @Override
    void progressComplete(String workflowId) {
        assert workflowId
        doEventComplete(workflowId)
    }

    protected doEventCreate(String workflowId) {
        assert workflowId
        loadStats.put(workflowId, new LoadStats(workflowId))
    }

    protected doEventUpdate(String workflowId, Map<Long,TaskLoad> tasks, List<Long> terminated) {
        assert workflowId
        final stats = loadStats
                .computeIfAbsent(workflowId, {new LoadStats(workflowId)})
                .update(tasks, terminated)
        loadStats.put(workflowId, stats)
    }

    protected doEventComplete(String workflowId) {
        assert workflowId
        final stats = loadStats.remove(workflowId)
        if( stats ) {
            updateLoadStats(stats)
        }
        else {
            log.error "Unable to find load stats for workflow Id=$workflowId"
        }
    }

    @Transactional(readOnly = true)
    ProgressData getProgress(Workflow workflow) {
        final progress = computeWorkflowProgress(workflow.id)

        final stats = loadStats.get(workflow.id)
        if( stats ) {
            progress.workflowProgress.withLoad(stats)
        }
        else if( workflow.checkIsComplete() ) {
            // return the peak values stored in the workflow entity
            progress.workflowProgress.peakCpus = workflow.peakLoadCpus ?: 0
            progress.workflowProgress.peakTasks = workflow.peakLoadTasks ?: 0
            progress.workflowProgress.peakMemory = workflow.peakLoadMemory ?: 0
        }
        else {
            log.warn "Unable to find load peak for workflow with Id=$workflow.id"
        }

        return progress
    }


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
        final workflowProgress = new WorkflowProgress()
        final aggregate = new LinkedHashMap<String,ProcessProgress>(20)
        for( List cols : tasks ) {
            final row = new ProgressRow(cols)
            // aggregate by process name
            def name = row.process
            def process = aggregate.get(name)
            if( process == null ) {
                process = new ProcessProgress(process: name)
                aggregate.put(name, process)
            }
            process.plus(row)
            
            // aggregate all nums for workflow
            workflowProgress.plus(row)
        }

        // aggregate workflow process
        final processProgresses = new ArrayList<ProcessProgress>(aggregate.values()).sort{it.process}
        new ProgressData(workflowProgress: workflowProgress, processesProgress: processProgresses)
    }

    protected void doPeriodicLoadCheck() {
        log.trace "Running periodic workflow load house cleaning"
        // delete stalled workflow execution
        // and delete if need
        final ids = findZombies(loadStats.values())
        if( ids ) {
            killZombies(ids)
        }
    }

    protected List<String> findZombies(Collection<LoadStats> loadStats) {
        loadStats
                .findAll { it.olderThan(aliveTimeout) }
                .collect { it.workflowId }
    }

    protected void killZombies(List<String> zombies) {
        log.warn "Unknown execution status for workflow=$zombies"
        for( String workflowId : zombies ) {
            loadStats.remove(workflowId)
            markWorkflowUnknownStatus(workflowId)
        }
    }

    protected void markWorkflowUnknownStatus(String workflowId) {
        try {
            log.debug "Marking workflow Id=$workflowId with unknown status"
            markWorkflowUnknownStatus0(workflowId)
        }
        catch( Exception e ) {
            log.error("Unable to save workflow with Id=$workflowId", e)
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void markWorkflowUnknownStatus0(String workflowId) {
        final workflow = Workflow.get(workflowId)
        if( workflow ) {
            workflow.status = WorkflowStatus.UNKNOWN
            workflow.save()
            // notify the status change
            liveEventsService.publishWorkflowEvent(workflow)
        }
    }

    protected updateLoadStats(LoadStats stats) {
        try {
            updateLoadStats0(stats)
        }
        catch( Exception e ) {
            log.error "Unable to update workflow peaks | ${e.message}"
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected updateLoadStats0(LoadStats stats) {
        def workflow = Workflow.get(stats.workflowId)
        if( !workflow ) {
            log.warn "Unable to update workflow peaks | Missing workflow id=$stats.workflowId"
            return
        }

        workflow.peakLoadCpus = stats.peakCpus
        workflow.peakLoadTasks = stats.peakTasks
        workflow.peakLoadMemory = stats.peakMemory
        workflow.save()
    }

}

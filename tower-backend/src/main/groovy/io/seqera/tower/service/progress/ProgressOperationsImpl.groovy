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

import javax.inject.Inject
import java.time.Duration

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.seqera.tower.domain.ProcessLoad
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.trace.TraceProgressData
import io.seqera.tower.exchange.trace.TraceProgressDetail
import org.springframework.transaction.annotation.Propagation
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
class ProgressOperationsImpl implements ProgressOperations {

    @Inject
    ProgressStore store

    /**
     * Initialise the progress data structure for the given workflow Id
     *
     * @param workflowId The Id of the workflow for which collect execution progress data
     * @param processNames The list of process names concurring this workflow
     */
    @Override
    void create(String workflowId, List<String> processNames) {
        log.debug("Creating progress state for workflow Id=$workflowId; processNames=$processNames")
        store.storeProgress(workflowId, new ProgressState(workflowId, processNames))
    }

    void updateProgress(String workflowId, TraceProgressData progressData) {
        assert workflowId
        assert progressData != null
        store.putTraceData(workflowId, progressData)
    }

    void aggregateMetrics(String workflowId, List<Task> tasks) {
        final executors = new HashSet()
        final terminated = new ArrayList(tasks.size())

        for( Task it : tasks ) {
            executors.add(it.executor)
            if( it.status.terminal )
                terminated.add(it)
        }
        
        store.updateStats(workflowId, executors, terminated)
    }
    
    /**
     * Update the workflow execution progress
     *
     * @param workflowId The workflow Id
     * @param tasks A list of {@link io.seqera.tower.domain.Task} instances that were carried out
     */
    @Deprecated
    @Override
    void updateStats(String workflowId, List<Task> tasks) {
        assert workflowId
        log.trace("Updating progress state for workflow Id=$workflowId; tasks count=${tasks.size()}")
        if( tasks ) {
            final ts = System.currentTimeMillis()
            final progress = store.getProgress(workflowId)
            if( !progress ) {
                log.warn "Missing workflow counters for Id=$workflowId"
                return
            }

            Map<Long, TaskStatus> tasksState = new HashMap<>(tasks.size())
            for( Task task : tasks ) {
                updateStats0(task, progress, tasksState)
            }

            // update the process peaks
            progress.updatePeaks()

            store.storeProgress(workflowId, progress)
            store.storeTaskStatuses(workflowId, tasksState)
            final delta = System.currentTimeMillis()-ts
            if( delta>1_000 )
                log.debug "++ Process stats completed for worflow id=$workflowId; delta=${delta}; count=${tasks.size()}"
        }
        else {
            // store an empty map to force the timestamp update
            store.storeTaskStatuses(workflowId, Collections.emptyMap())
        }
    }

    private void updateStats0(Task task, ProgressState progress, Map<Long,TaskStatus> state) {

        // track the executor uses
        if( task.executor )
            progress.executors.add( task.executor )

        /*
         * check the previous status
         */
        final previous = (state.containsKey(task.id)
                ? state.get(task.id)
                : store.getTaskStatus(progress.workflowId, task.id))

        final process = progress.getState(task.process)

        /*
         * decrement the counter the task in the previous status
         */
        if( previous ) {
            try { process.decStatus(previous) }
            catch( Exception e ) { log.warn "${e.message} | workflowId=$progress.workflowId; taskId=$task.id" }
        }

        /*
         * increment the counter for the current status
         */
        process.incStatus(task.status)

        /*
         * when the task is in running status update the load and peak counters
         */
        if( task.status.isRunning() ) {
            process.incLoad(task)
        }

        /*
         * decrement the load if the execution is terminated
         */
        if( task.status.isTerminal() ) {
            // if there's a status transition from RUNNING to DONE
            // decrement the load for the task
            if( previous == TaskStatus.RUNNING ) {
                try { process.decLoad(task) }
                catch( Exception e ) { log.warn "${e.message} | workflowId=$progress.workflowId; taskId=$task.id" }
            }

            // increment the stats for the task
            process.incStats(task)

            // remove this task status
            state.put(task.id, null)
        }
        /* keep track of this task status */
        else {
            state.put(task.id, task.status)
        }

    }

    ProgressData computeStats0(String workflowId, TraceProgressData trace) {
        assert trace

        final load = store.getWorkflowLoad(workflowId) ?: new WorkflowLoad()
        load.pending = trace.pending
        load.submitted = trace.submitted
        load.running = trace.running
        load.succeeded = trace.succeeded
        load.failed = trace.failed
        load.cached = trace.cached
        // load and peaks
        load.loadCpus = trace.loadCpus
        load.loadMemory = trace.loadMemory
        load.peakTasks = trace.peakRunning
        load.peakCpus = trace.peakCpus
        load.peakMemory = trace.peakMemory

        final processes = new ArrayList<ProcessLoad>(trace.processes.size())
        for(TraceProgressDetail detail : trace.processes ) {
            final item = new ProcessLoad()
            item.process = detail.name
            item.pending = detail.pending
            item.submitted = detail.submitted
            item.running = detail.running
            item.succeeded = detail.succeeded
            item.failed = detail.failed
            item.cached = detail.cached
            // load and peaks
            item.loadCpus = detail.loadCpus
            item.loadMemory = detail.loadMemory
            item.peakTasks = detail.peakRunning
            item.peakCpus = detail.peakCpus
            item.peakMemory = detail.peakMemory

            processes.add(item)
        }

        new ProgressData( workflowProgress: load, processesProgress: processes )
    }

    ProgressData computeStats(String workflowId) {
        def trace = store.getTraceData(workflowId)
        if( trace ) {
            return computeStats0(workflowId, trace)
        }

        // fallback mechanism
        ProgressState progress = store.getProgress(workflowId)
        progress ? new ProgressData(
                workflowProgress: progress.workflow,
                processesProgress: progress.processLoads ) : null
    }

    @Override
    ProgressData getProgressData(String workflowId) {
        return computeStats(workflowId) ?: load(workflowId)
    }

    /**
     * Find the Ids of workflow executions timed-out
     *
     * @param duration
     *      A time duration after which the workflow for which no data is received
     *      is considered timed-out
     * @return
     *      A list of workflow Id strings
     */
    List<String> findExpired(Duration duration) {
        store.findExpiredKeys(duration)
    }

    /**
     * Load a previously persisted workflow execution progress
     *
     * @param workflowId
     *      The workflow Id
     * @return
     *      The {@link ProgressData} instance holding process
     *      and workflow execution progress metadata
     */
    ProgressData load(String workflowId) {
        final params = [workflowId: workflowId]
        final workflow = WorkflowLoad.find('from WorkflowLoad p where p.workflow.id=:workflowId',params)
        if( workflow == null )
            return null

        final query = '''
                select p
                from WorkflowProcess w
                join fetch ProcessLoad p 
                  on p.process = w.name and p.workflow = w.workflow
                where 
                  w.workflow.id = :workflowId
                order by 
                    w.position 
            '''
        final list = ProcessLoad.executeQuery(query, params)
        new ProgressData(processesProgress: list, workflowProgress: workflow)
    }

    /**
     * Save the progress data to long term DB once
     * the workflow execution terminated
     *
     * @param workflowId The workflow Id
     */
    @Deprecated void complete(String workflowId) {
        log.trace("Completing progress state for workflow Id=$workflowId")
        persistProgressData(workflowId)
        store.deleteData(workflowId)
    }

    /*
     * persist logic
     */
    void persistProgressData(String workflowId) {
        try {
            final data = computeStats(workflowId)
            if( data )
                persistProgressData(workflowId, data)
            else
                log.error "Unable to persists progress stats for workflow=$workflowId -- Missing stats data"
        }
        catch( Exception e ) {
            log.error "Unable to persists progress stats for workflow=$workflowId", e
        }
    }

    @CompileDynamic
    @Transactional(propagation = Propagation.REQUIRED)
    void persistProgressData(String workflowId, ProgressData data) {
        log.trace "Persisting workflow stats=$data"
        final workflow = Workflow.get(workflowId)
        persistProgressData(workflow, data)
    }

    void persistProgressData(Workflow workflow, ProgressData data) {
        for( ProcessLoad process : data.processesProgress ) {
            process.workflow = workflow
            process.save(failOnError:true)
        }

        data.workflowProgress.workflow = workflow
        data.workflowProgress.save(failOnError:true)
    }

    List<ProgressState> getStats() {
        def keys = store.getAllKeys()
        def result = new ArrayList(keys.size())
        for( String k : keys ) {
            result << store.getProgress(k)
        }
        return result
    }

}

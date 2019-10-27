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
import io.seqera.tower.domain.WorkflowProcess
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.progress.ProgressData
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
        log.trace("Creating progress state for workflow Id=$workflowId")
        store.storeProgress(workflowId, new ProgressState(workflowId, processNames))
    }

    /**
     * Update the workflow execution progress
     *
     * @param workflowId The workflow Id
     * @param tasks A list of {@link io.seqera.tower.domain.Task} instances that were carried out
     */
    @Override
    void updateStats(String workflowId, List<Task> tasks) {
        assert workflowId
        log.trace("Updating progress state for workflow Id=$workflowId; tasks count=${tasks.size()}")

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
    }

    private void updateStats0(Task task, ProgressState progress, Map<Long,TaskStatus> state) {

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
            process.decStatus(previous)
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
        if( task.status.isTerminated() ) {
            // if there's a status transition from RUNNING to DONE
            // decrement the load for the task
            if( previous == TaskStatus.RUNNING ) {
                process.decLoad(task)
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

    ProgressData computeStats(String workflowId) {
        ProgressState progress = store.getProgress(workflowId)
        progress ? new ProgressData(
                workflowProgress: progress.workflow,
                processesProgress: progress.processes ) : null
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
        def query = '''
                select p
                from WorkflowProcess w
                join fetch ProcessLoad p on p.process = w.name
                where 
                  w.workflow.id = :workflowId
                order by 
                    w.position 
            '''
        final params = [workflowId: workflowId]
        final list = ProcessLoad.executeQuery(query, params)
        final workflow = (WorkflowLoad) WorkflowProcess.executeQuery('from WorkflowLoad p where p.workflow.id=:workflowId',params) [0]
        new ProgressData(processesProgress: list, workflowProgress: workflow)
    }

    /**
     * Save the progress data to long term DB once
     * the workflow execution terminated
     *
     * @param workflowId The workflow Id
     */
    void complete(String workflowId) {
        log.trace("Completing progress state for workflow Id=$workflowId")
        persist0(workflowId)
        store.deleteProgress(workflowId)
    }

    /*
     * persist logic
     */
    protected void persist0(String workflowId) {
        final data = computeStats(workflowId)
        try {
            persist0(workflowId, data)
        }
        catch( Exception e ) {
            log.warn "Unable to persists workflow progress stats=${data} | ${e.message}"
        }
    }

    @CompileDynamic
    @Transactional(propagation = Propagation.REQUIRED)
    protected void persist0(String workflowId, ProgressData data) {
        log.trace "Persisting workflow stats=$data"
        final workflow = Workflow.get(workflowId)

        for( ProcessLoad process : data.processesProgress ) {
            process.workflow = workflow
            process.save()
        }

        data.workflowProgress.workflow = workflow
        data.workflowProgress.save()
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

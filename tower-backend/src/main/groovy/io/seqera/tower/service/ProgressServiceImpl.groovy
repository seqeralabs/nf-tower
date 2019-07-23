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

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.tower.domain.ProcessProgress
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.TasksProgress
import io.seqera.tower.domain.WorkflowTasksProgress
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.progress.ProgressGet
import io.seqera.tower.exchange.workflow.WorkflowGet

import javax.inject.Singleton

@Transactional
@Singleton
class ProgressServiceImpl implements ProgressService {

    @CompileDynamic  // <-- TODO make this static removing the `findAllByWorkflow` dynamic finder 
    WorkflowGet buildWorkflowGet(Workflow workflow) {
        WorkflowGet result = new WorkflowGet(workflow: workflow)

        if (workflow.checkIsStarted()) {
            result.progress = computeWorkflowProgress(workflow.id)
        } else {
            result.progress = new ProgressGet(workflowTasksProgress: workflow.tasksProgress, processesProgress: workflow.processesProgress.sort { it.process })
            result.summary = WorkflowMetrics.findAllByWorkflow(workflow)
        }

        return result
    }

    ProgressGet computeWorkflowProgress(Long workflowId) {
        new ProgressGet(workflowTasksProgress: computeTasksProgress(workflowId), processesProgress: computeProcessesProgress(workflowId))
    }

    @CompileDynamic
    private WorkflowTasksProgress computeTasksProgress(Long workflowId) {
        List<Object[]> tuples = new DetachedCriteria(Task).build {
            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('status')
                countDistinct('id')
            }
        }.list()

        Map<String, Long> progressProperties = tuples.collectEntries { Object[] tuple ->
            [( ((TaskStatus) tuple[0]).toProgressTag()): (Long) tuple[1]]
        }

        TasksProgress progress = new TasksProgress(progressProperties)
        new WorkflowTasksProgress(progress: progress)
    }

    @CompileDynamic
    private List<ProcessProgress> computeProcessesProgress(Long workflowId) {
        Map<String, Map<TaskStatus, List<Map>>> statusCountByProcess = queryProcessesTasksStatus(workflowId)

        statusCountByProcess.collect { String process, Map<TaskStatus, List<Map>> statusCountsOfProcess ->
            TasksProgress progress = new TasksProgress()

            statusCountsOfProcess.each { TaskStatus status, List<Map> countOfProcess ->
                progress[status.toProgressTag()] = countOfProcess.first().count
            }

            new ProcessProgress(process: process, progress: progress)
        }.sort { it.process }
    }

    @CompileDynamic
    private Map<String, Map> queryProcessesTasksStatus(Long workflowId) {
        List<Object[]> tuples = new DetachedCriteria(Task).build {
            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('process')
                groupProperty('status')
                countDistinct('id')
            }
        }.list()

        Map<String, Map<TaskStatus, List<Map>>> statusCountByProcess = tuples.collect { Object[] tuple ->
            [process: tuple[0], status: tuple[1], count: tuple[2]]
        }.groupBy([
            { Map data -> data.process },
            { Map data -> data.status },
        ])

        statusCountByProcess
    }

}

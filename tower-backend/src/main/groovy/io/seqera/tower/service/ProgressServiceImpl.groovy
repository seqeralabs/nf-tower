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

import java.time.OffsetDateTime

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.tower.domain.ProcessProgress
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.TasksProgress
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.progress.ProgressGet
import io.seqera.tower.exchange.workflow.WorkflowGet

import javax.inject.Singleton
import java.time.Instant

@Transactional
@Singleton
class ProgressServiceImpl implements ProgressService {

    @CompileDynamic  // <-- TODO make this static removing the `findAllByWorkflow` dynamic finder 
    WorkflowGet buildWorkflowGet(Workflow workflow) {
        WorkflowGet result = new WorkflowGet(workflow: workflow)

        if (workflow.checkIsStarted()) {
            result.progress = computeWorkflowProgress(workflow.id)
        } else {
            result.progress = new ProgressGet(tasksProgress: workflow.tasksProgress, processesProgress: workflow.processesProgress.sort { it.process })
            result.summary = WorkflowMetrics.findAllByWorkflow(workflow)
        }

        return result
    }

    ProgressGet computeWorkflowProgress(Long workflowId) {
        new ProgressGet(tasksProgress: computeTasksProgress(workflowId), processesProgress: computeProcessesProgress(workflowId))
    }

    @CompileDynamic
    private TasksProgress computeTasksProgress(Long workflowId) {
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
            [(tuple[0].toProgressString()): tuple[1]]
        }
        new TasksProgress(progressProperties)
    }

    private List<ProcessProgress> computeProcessesProgress(Long workflowId) {
        Map<String, Long> totalCountByProcess = queryProcessesTasksStatus(workflowId)
        Map<String, Long> completedCountByProcess = queryProcessesTasksStatus(workflowId, TaskStatus.COMPLETED)
        Map<String, Long> totalDurationByProcess = queryProcessesTotalDuration(workflowId)
        Map<String, String> lastSubmittedTaskHashByProcess = queryProcessesLastSubmittedTaskHash(workflowId)

        totalCountByProcess.collect { String process, Long totalCount ->
            new ProcessProgress(
                    process: process, totalTasks: totalCount, completedTasks: completedCountByProcess[process] ?: 0,
                    totalDuration: totalDurationByProcess[process] ?: 0, lastTaskHash: lastSubmittedTaskHashByProcess[process]
            )
        }.sort { it.process }
    }

    @CompileDynamic
    private Map<String, Long> queryProcessesTasksStatus(Long workflowId, TaskStatus status = null) {
        List<Object[]> tuples = new DetachedCriteria(Task).build {
            if (status) {
                eq('status', status)
            }

            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('process')
                countDistinct('id')
            }
        }.list()

        Map<String, Long> statusCountByProcess = tuples.collectEntries { Object[] tuple ->
            [(tuple[0]): tuple[1]]
        }
        statusCountByProcess
    }

    @CompileDynamic
    private Map<String, Long> queryProcessesTotalDuration(Long workflowId) {
        List<Object[]> tuples = new DetachedCriteria(Task).build {
            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('process')
                sum('duration')
            }
        }.list()

        Map<String, Long> totalDurationByProcess = tuples.collectEntries { Object[] tuple ->
            [(tuple[0]): tuple[1]]
        }
        totalDurationByProcess
    }

    @CompileDynamic
    private Map<String, String> queryProcessesLastSubmittedTaskHash(Long workflowId) {
        List<Object[]> tuples = new DetachedCriteria(Task).build {
            workflow {
                eq('id', workflowId)
            }

            projections {
                groupProperty('process')
                max('submit')
            }
        }.list()
        Map<String, Instant> lastSubmitTimeTaskByProcess = tuples.collectEntries { Object[] tuple ->
            [(tuple[0]): tuple[1]]
        }

        Map<String, String> lastSubmittedTaskHashByProcess = lastSubmitTimeTaskByProcess.collectEntries { String process, OffsetDateTime submitTime ->
            String hash = new DetachedCriteria(Task).build {
                workflow {
                    eq('id', workflowId)
                }
                eq('process', process)
                eq('submit', submitTime)

                projections {
                    property('hash')
                }
            }.get()

            [(process): hash]
        }

        lastSubmittedTaskHashByProcess
    }
}

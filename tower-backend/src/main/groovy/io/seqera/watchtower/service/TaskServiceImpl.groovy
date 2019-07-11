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

package io.seqera.watchtower.service

import javax.inject.Inject
import javax.inject.Singleton

import grails.gorm.DetachedCriteria
import grails.gorm.PagedResultList
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.enums.TaskStatus
import io.seqera.watchtower.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.exchange.trace.TraceTaskRequest

@Transactional
@Singleton
class TaskServiceImpl implements TaskService {

    WorkflowService workflowService

    @Inject
    TaskServiceImpl(WorkflowService workflowService) {
        this.workflowService = workflowService
    }

    List<Task> processTaskTraceRequest(TraceTaskRequest request) {
        request.tasks.collect { Task task ->
            saveTask(task, request.workflowId)
        }
    }

    @CompileDynamic
    private Task saveTask(Task task, String workflowId) {
        Workflow existingWorkflow = Workflow.get(workflowId)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't find workflow associated with the task")
        }

        Task existingTask = Task.findByWorkflowAndTaskId(existingWorkflow, task.taskId)
        if (existingTask) {
            updateChangeableFields(task, existingTask)
            task = existingTask
        } else {
            task.workflow = existingWorkflow
        }

        task.save()
        return task
    }

    private void updateChangeableFields(Task originalTask, Task taskToUpdate) {
        taskToUpdate.status = originalTask.status
        taskToUpdate.start = originalTask.start
        taskToUpdate.complete = originalTask.complete
        taskToUpdate.duration = originalTask.duration

        taskToUpdate.realtime = originalTask.realtime
        taskToUpdate.pcpu = originalTask.pcpu
        taskToUpdate.rchar = originalTask.rchar
        taskToUpdate.wchar = originalTask.wchar
        taskToUpdate.syscr = originalTask.syscr
        taskToUpdate.syscw = originalTask.syscw
        taskToUpdate.readBytes = originalTask.readBytes
        taskToUpdate.writeBytes = originalTask.writeBytes
        taskToUpdate.pmem = originalTask.pmem
        taskToUpdate.vmem = originalTask.vmem
        taskToUpdate.rss = originalTask.rss
        taskToUpdate.peakVmem = originalTask.peakVmem
        taskToUpdate.peakRss = originalTask.peakRss
        taskToUpdate.volCtxt = originalTask.volCtxt
        taskToUpdate.invCtxt = originalTask.invCtxt

        taskToUpdate.errorAction = originalTask.errorAction
    }

    @CompileDynamic
    PagedResultList<Task> findTasks(Long workflowId, Long max, Long offset, String orderProperty, String orderDirection, String sqlRegex) {
        Collection<TaskStatus> statusesToSearch = sqlRegex ? TaskStatus.findStatusesByRegex(sqlRegex) : null

        new DetachedCriteria<Task>(Task).build {
            workflow {
                eq('id', workflowId)
            }

            if (sqlRegex) {
                or {
                    ilike('process', sqlRegex)
                    ilike('tag', sqlRegex)
                    ilike('hash', sqlRegex)

                    if (statusesToSearch) {
                        'in'('status', statusesToSearch)
                    }
                }
            }

            order(orderProperty, orderDirection)
        }.list(max: max, offset: offset)
    }

}
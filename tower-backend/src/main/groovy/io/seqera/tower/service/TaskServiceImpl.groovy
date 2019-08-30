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

import javax.inject.Inject
import javax.inject.Singleton

import grails.gorm.DetachedCriteria
import grails.gorm.PagedResultList
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exceptions.NonExistingWorkflowException
import io.seqera.tower.exchange.trace.TraceTaskRequest

@Transactional
@Singleton
class TaskServiceImpl implements TaskService {

    WorkflowService workflowService

    @Inject
    TaskServiceImpl(WorkflowService workflowService) {
        this.workflowService = workflowService
    }

    List<Task> processTaskTraceRequest(TraceTaskRequest request) {
        Workflow existingWorkflow = Workflow.get(request.workflowId)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't find task for workflow ID: ${request.workflowId   }")
        }

        request.tasks.collect { Task task -> saveTask(task, existingWorkflow) }
    }

    @CompileDynamic
    private Task saveTask(Task task, Workflow existingWorkflow) {

        Task existingTask = Task.findByWorkflowAndTaskId(existingWorkflow, task.taskId)
        if (existingTask) {
            updateMutableFields(existingTask, task)
            existingTask.save()
            return existingTask
        }
        else {
            task.workflow = existingWorkflow
            task.save()
            return task
        }

    }

    private void updateMutableFields(Task taskToUpdate, Task originalTask) {
        taskToUpdate.status = originalTask.status

        taskToUpdate.submit = originalTask.submit
        taskToUpdate.start = originalTask.start
        taskToUpdate.complete = originalTask.complete

        taskToUpdate.module = originalTask.module
        taskToUpdate.container = originalTask.container
        taskToUpdate.attempt = originalTask.attempt
        taskToUpdate.script = originalTask.script
        taskToUpdate.scratch = originalTask.scratch
        taskToUpdate.workdir = originalTask.workdir
        taskToUpdate.queue = originalTask.queue
        taskToUpdate.cpus = originalTask.cpus
        taskToUpdate.memory = originalTask.memory
        taskToUpdate.disk = originalTask.disk
        taskToUpdate.time = originalTask.time
        taskToUpdate.env = originalTask.env

        taskToUpdate.errorAction = originalTask.errorAction
        taskToUpdate.exitStatus = originalTask.exitStatus
        taskToUpdate.duration = originalTask.duration
        taskToUpdate.realtime = originalTask.realtime
        taskToUpdate.nativeId = originalTask.nativeId
        taskToUpdate.pcpu = originalTask.pcpu
        taskToUpdate.pmem = originalTask.pmem
        taskToUpdate.rss = originalTask.rss
        taskToUpdate.vmem = originalTask.vmem
        taskToUpdate.peakRss = originalTask.peakRss
        taskToUpdate.peakVmem = originalTask.peakVmem
        taskToUpdate.rchar = originalTask.rchar
        taskToUpdate.wchar = originalTask.wchar
        taskToUpdate.syscr = originalTask.syscr
        taskToUpdate.syscw = originalTask.syscw
        taskToUpdate.readBytes = originalTask.readBytes
        taskToUpdate.writeBytes = originalTask.writeBytes
        taskToUpdate.volCtxt = originalTask.volCtxt
        taskToUpdate.invCtxt = originalTask.invCtxt
    }

    @CompileDynamic
    PagedResultList<Task> findTasks(String workflowId, Long max, Long offset, String orderProperty, String orderDirection, String sqlRegex) {
        def statusesToSearch = TaskStatus.findStatusesByRegex(sqlRegex)

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

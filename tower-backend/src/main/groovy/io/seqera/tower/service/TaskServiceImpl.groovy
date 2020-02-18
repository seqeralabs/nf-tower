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

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.TaskData
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exceptions.NonExistingWorkflowException
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.service.audit.AuditEventPublisher
import io.seqera.util.TupleUtils

@Slf4j
@Transactional
@Singleton
@CompileStatic
class TaskServiceImpl implements TaskService {

    @Inject WorkflowService workflowService
    @Inject AuditEventPublisher auditEventPublisher

    @CompileDynamic
    TaskData getTaskDataBySessionIdAndHash(String sessionId, String hash) {
        return TaskData.findBySessionIdAndHash(sessionId, hash)
    }

//    TaskData getDataBySessionIdAndHash(String sessionId, String hash) {
//        def result = Task.executeQuery("""\
//                from
//                  TaskData d
//                where
//                  d.id in (
//                    select t.data.id
//                    from
//                      Task t, Workflow w
//                    where
//                      t.workflow = w
//                      and w.sessionId = :sessionId
//                  )
//                  and d.hash = :hash
//                """, [hash: hash, sessionId: sessionId])
//        if( !result )
//            return null
//        if( result.size()>1 )
//            throw new IllegalStateException()
//        return (TaskData)result.get(0)
//    }


    @Deprecated
    List<Task> processTaskTraceRequest(TraceTaskRequest request) {
        final Workflow workflow = Workflow.get(request.workflowId)
        if (!workflow) {
            throw new NonExistingWorkflowException("Can't find task for workflow Id: ${request.workflowId}")
        }
        // mark it for running
        workflowService.markForRunning(request.workflowId)
        // save all the acquired tasks
        request.tasks.collect { Task task -> saveTask(task, workflow) }
    }

    @CompileDynamic
    Task saveTask(Task task, Workflow workflow) {

        // task `cached` are submitted just one time
        TaskData record
        if( task.checkIsCached() && (record=getTaskDataBySessionIdAndHash(workflow.sessionId, task.hash)) ) {
            // if the data record already is stored, load and link it to the task instance
            task.data = record
            // save the task
            task.workflow = workflow
            task.save()
            return task
        }

        Task existingTask = Task.findByWorkflowAndTaskId(workflow, task.taskId)
        if (existingTask) {
            updateMutableFields(existingTask, task)
            existingTask.save()
            return existingTask
        }
        else {
            task.workflow = workflow
            task.data.sessionId = workflow.sessionId
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
        taskToUpdate.executor = originalTask.executor
        taskToUpdate.machineType = originalTask.machineType
        taskToUpdate.cloudZone = originalTask.cloudZone
        taskToUpdate.priceModel = originalTask.priceModel
        taskToUpdate.cost = originalTask.cost
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

    private static List<String> TASK_DATA_SORTABLE_FIELDS = ['submit','duration','realtime','peakRss','peakVmem','rchar','wchar','volCtxt','invCtxt']

    private static List<String> ORDER_DIRECTION = ['asc','desc','ASC','DESC']

    @CompileDynamic
    List<Task> findTasks(String workflowId, String filter, String orderProperty, String orderDirection, Long max, Long offset) {
        log.trace "findTasks workflowId=$workflowId; max=$max; offset=$offset; filter=$filter; orderProperty=$orderProperty; orderDirection=$orderDirection"
        if( !(orderDirection in ORDER_DIRECTION))
            throw new IllegalArgumentException("Invalid order direction: $orderDirection")

        def params = [workflowId: workflowId]
        def query = createTaskQuery0(params, filter)
        if( orderProperty ) {
            if( orderProperty == 'taskId')
                orderProperty  = "t.${orderProperty}"
            else if( orderProperty in TASK_DATA_SORTABLE_FIELDS )
                orderProperty  = "d.${orderProperty}"
            else
                throw new IllegalStateException("Invalid Task sort field: $orderProperty")
            query += " order by $orderProperty ${orderDirection.toLowerCase()}".toString()
        }

        final args = [max: max, offset: offset, fetchSize: max]
        Task.executeQuery(query, params, args)
    }

    long countTasks(String workflowId, String filter) {
        def params = [workflowId: workflowId]
        def query = createTaskQuery0(params, filter, true)
        def result = Task.executeQuery(query, params)
        return result[0] as long
    }

    @Override
    Task findByTaskId(Long taskId) {
        final params = TupleUtils.map('taskId', taskId)
        final query = "from Task t where t.id = :taskId"
        Task.find(query, params)
    }

    private String createTaskQuery0(Map params, String search, boolean count=false) {
        final statusesToSearch = TaskStatus.findStatusesByRegex(search)
        final String filter = search ? search.contains('*') ? search.replaceAll(/\*/, '%') : "${search}%".toString() : null

        def query = (count ?
                """\
                select count(t.id)
                from Task t
                join t.data d
                where t.workflow.id = :workflowId 
                """
            :
                """\
                select t
                from Task t
                join fetch t.data d
                where t.workflow.id = :workflowId 
                """ ).stripIndent()

        if( filter )
            params.filter = filter.toLowerCase()
        if( statusesToSearch  )
            params.statuses = statusesToSearch

        if( params.filter ) {
            query += " and ("
            query += "lower(d.process) like :filter or lower(d.tag) like :filter or lower(d.hash) like :filter"
            if( params.statuses )
                query += " or t.status in :statuses"
            query += ")"
        }

        return query
    }

}

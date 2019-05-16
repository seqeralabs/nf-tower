package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.controller.TraceWorkflowRequest
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exceptions.NonExistingTaskException
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException

import javax.inject.Inject
import javax.inject.Singleton

@Transactional
@Singleton
class TaskServiceImpl implements TaskService {

    WorkflowService workflowService

    @Inject
    TaskServiceImpl(WorkflowService workflowService) {
        this.workflowService = workflowService
    }

    Task processTaskJsonTrace(TraceWorkflowRequest trace) {
        trace.task.checkIsSubmitted() ? createFromJson(trace.task, trace.progress) : updateFromJson(trace.task, trace.progress)
    }


    @CompileDynamic
    private Task createFromJson(Task task, Progress progress) {
        Workflow existingWorkflow = Workflow.get(task.relatedWorkflowId)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't create task associated with non existing workflow")
        }

        existingWorkflow.progress = progress
        task.workflow = existingWorkflow

        existingWorkflow.save()
        task.save()
        task
    }

    @CompileDynamic
    private Task updateFromJson(Task task, Progress progress) {
        Workflow existingWorkflow = Workflow.get(task.relatedWorkflowId)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't find workflow associated with the task")
        }

        Task existingTask = Task.findByWorkflowAndTaskId(existingWorkflow, task.taskId)
        if (!existingTask) {
            throw new NonExistingTaskException("Can't update a non existing task")
        }

        existingWorkflow.progress = progress
        updateChangeableFields(task, existingTask)

        existingWorkflow.save()
        existingTask.save()
        existingTask
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

}
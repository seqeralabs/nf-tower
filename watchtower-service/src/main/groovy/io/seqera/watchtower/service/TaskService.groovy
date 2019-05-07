package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.TaskTraceJsonUnmarshaller
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.exceptions.NonExistingTaskException
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException

import javax.inject.Inject
import javax.inject.Singleton

@Transactional
@Singleton
class TaskService {

    WorkflowService workflowService

    @Inject
    TaskService(WorkflowService workflowService) {
        this.workflowService = workflowService
    }

    Task processTaskJsonTrace(Map taskJson) {
        TaskStatus taskStatus = TaskTraceJsonUnmarshaller.identifyTaskStatus(taskJson)

        taskStatus == TaskStatus.SUBMITTED ? createFromJson(taskJson) : updateFromJson(taskJson, taskStatus)
    }


    @CompileDynamic
    Task createFromJson(Map taskJson) {
        Workflow existingWorkflow = Workflow.get((Long) taskJson.task['workflowId'])
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't create task associated with non existing workflow")
        }

        Task newTask = new Task(workflow: existingWorkflow)
        TaskTraceJsonUnmarshaller.populateTaskFields(taskJson, TaskStatus.SUBMITTED, newTask)

        newTask.save()
        newTask
    }

    @CompileDynamic
    Task updateFromJson(Map taskJson, TaskStatus taskStatus) {
        Workflow existingWorkflow = Workflow.get((Long) taskJson.task['workflowId'])
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't find workflow associated with the task")
        }

        Task existingTask = Task.findByWorkflowAndTaskId(existingWorkflow, (Long) taskJson.task['taskId'])
        if (!existingTask) {
            throw new NonExistingTaskException("Can't update a non existing task")
        }

        TaskTraceJsonUnmarshaller.populateTaskFields(taskJson, taskStatus, existingTask)

        existingTask.save()
        existingTask
    }

}
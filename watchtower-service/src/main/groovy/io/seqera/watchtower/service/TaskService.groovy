package io.seqera.watchtower.service

import grails.gorm.services.Service
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.TaskTraceJsonUnmarshaller
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.exceptions.NonExistingTaskException
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Service(Task)
abstract class TaskService {

    @Inject
    WorkflowService workflowService


    Task processTaskJsonTrace(Map taskJson) {
        TaskStatus taskStatus = TaskTraceJsonUnmarshaller.identifyTaskStatus(taskJson)

        taskStatus == TaskStatus.SUBMITTED ? createFromJson(taskJson) : updateFromJson(taskJson, taskStatus)
    }

    Task createFromJson(Map taskJson) {
        Workflow existingWorkflow = workflowService.get((String) taskJson.runId, (String) taskJson.runName)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't create task associated with non existing workflow")
        }

        Task newTask = new Task(workflow: existingWorkflow)
        TaskTraceJsonUnmarshaller.populateTaskFields(taskJson, TaskStatus.SUBMITTED, newTask)

        newTask.save()
        newTask
    }

    Task updateFromJson(Map taskJson, TaskStatus taskStatus) {
        Workflow existingWorkflow = workflowService.get((String) taskJson.runId, (String) taskJson.runName)
        Task existingTask = Task.findByWorkflowAndTask_id(existingWorkflow, (Long) taskJson.trace['task_id'])
        if (!existingTask) {
            throw new NonExistingTaskException("Can't update a non-existing workflow")
        }

        TaskTraceJsonUnmarshaller.populateTaskFields(taskJson, taskStatus, existingTask)

        existingTask.save()
        existingTask
    }

}
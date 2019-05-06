package io.seqera.watchtower.service


import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
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

    @Inject
    WorkflowService workflowService


    Task processTaskJsonTrace(Map taskJson) {
        TaskStatus taskStatus = TaskTraceJsonUnmarshaller.identifyTaskStatus(taskJson)

        taskStatus == TaskStatus.SUBMITTED ? createFromJson(taskJson) : updateFromJson(taskJson, taskStatus)
    }


    @TypeChecked(value = TypeCheckingMode.SKIP)
    Task createFromJson(Map taskJson) {
        Workflow existingWorkflow = Workflow.findByRunIdAndRunName((String) taskJson.runId, (String) taskJson.runName)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't create task associated with non existing workflow")
        }

        Task newTask = new Task(workflow: existingWorkflow)
        TaskTraceJsonUnmarshaller.populateTaskFields(taskJson, TaskStatus.SUBMITTED, newTask)

        newTask.save()
        newTask
    }

    @TypeChecked(value = TypeCheckingMode.SKIP)
    Task updateFromJson(Map taskJson, TaskStatus taskStatus) {
        Workflow existingWorkflow = Workflow.findByRunIdAndRunName((String) taskJson.runId, (String) taskJson.runName)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't find workflow associated with the task")
        }

        Task existingTask = Task.findByWorkflowAndTask_id(existingWorkflow, (Long) taskJson.trace['task_id'])
        if (!existingTask) {
            throw new NonExistingTaskException("Can't update a non existing task")
        }

        TaskTraceJsonUnmarshaller.populateTaskFields(taskJson, taskStatus, existingTask)

        existingTask.save()
        existingTask
    }

}
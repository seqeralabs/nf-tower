package io.seqera.watchtower.service


import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.exceptions.NonExistingTaskException
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.DomainCreator
import io.seqera.watchtower.util.TracesJsonBank

import javax.inject.Inject

@MicronautTest(application = Application.class)
class TaskServiceSpec extends AbstractContainerBaseSpec {

    @Inject
    TaskService taskService


    void "submit a task given a submit trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task JSON submitted trace"
        Map taskTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        when: "unmarshall the JSON to a task"
        Task task
        Task.withNewTransaction {
            task = taskService.processTaskJsonTrace(taskTraceJson)
        }

        then: "the task has been correctly saved"
        task.id
        task.currentStatus == TaskStatus.SUBMITTED
        task.submitTime
        !task.startTime
        !task.completeTime
        Task.count() == 1
    }

    void "submit a task given a submit trace, then start the task given a start trace, last complete the task given a success trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        and: 'a task started trace'
        Map taskStartedTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.STARTED)

        and: 'a task succeeded trace'
        Map taskSucceededTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUCCEEDED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted
        Task.withNewTransaction {
            taskSubmitted = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the workflow has been correctly saved"
        taskSubmitted.id
        taskSubmitted.currentStatus == TaskStatus.SUBMITTED
        taskSubmitted.submitTime
        !taskSubmitted.startTime
        !taskSubmitted.completeTime
        Task.count() == 1

        when: "unmarshall the started task trace"
        Task taskStarted
        Task.withNewTransaction {
            taskStarted = taskService.processTaskJsonTrace(taskStartedTrace)
        }

        then: "the task has been started"
        taskSubmitted.id == taskStarted.id
        taskStarted.currentStatus == TaskStatus.STARTED
        taskStarted.submitTime
        taskStarted.startTime
        !taskStarted.completeTime
        Task.count() == 1

        when: "unmarshall the succeeded task trace"
        Task taskCompleted
        Task.withNewTransaction {
            taskCompleted = taskService.processTaskJsonTrace(taskSucceededTraceJson)
        }

        then: "the task has been started"
        taskSubmitted.id == taskCompleted.id
        taskStarted.currentStatus == TaskStatus.SUCCEEDED
        taskStarted.submitTime
        taskStarted.startTime
        taskStarted.completeTime
        Task.count() == 1
    }

    void "submit a task given a submit trace, then start the task given a start trace, last complete the task given a fail trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        and: 'a task started trace'
        Map taskStartedTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.STARTED)

        and: 'a task succeeded trace'
        Map taskFailedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.FAILED)



        when: "unmarshall the JSON to a task"
        Task taskSubmitted
        Task.withNewTransaction {
            taskSubmitted = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the workflow has been correctly saved"
        taskSubmitted.id
        taskSubmitted.currentStatus == TaskStatus.SUBMITTED
        taskSubmitted.submitTime
        !taskSubmitted.startTime
        !taskSubmitted.completeTime
        Task.count() == 1



        when: "unmarshall the started task trace"
        Task taskStarted
        Task.withNewTransaction {
            taskStarted = taskService.processTaskJsonTrace(taskStartedTrace)
        }

        then: "the task has been started"
        taskSubmitted.id == taskStarted.id
        Task.count()

        taskStarted.currentStatus == TaskStatus.STARTED
        taskStarted.submitTime
        taskStarted.startTime
        !taskStarted.completeTime

        when: "unmarshall the succeeded task trace"
        Task taskCompleted
        Task.withNewTransaction {
            taskCompleted = taskService.processTaskJsonTrace(taskFailedTraceJson)
        }

        then: "the task has been started"
        taskSubmitted.id == taskCompleted.id
        taskStarted.currentStatus == TaskStatus.FAILED
        taskStarted.submitTime
        taskStarted.startTime
        taskStarted.completeTime
        taskStarted.errorAction
        Task.count() == 1
    }

    void "submit a task given a submit trace, then try to submit the same one"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted1
        Task.withNewTransaction {
            taskSubmitted1 = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the task has been correctly saved"
        taskSubmitted1.id
        taskSubmitted1.currentStatus == TaskStatus.SUBMITTED
        taskSubmitted1.submitTime
        Task.count() == 1

        when: "unmarshall the submit JSON to a second task"
        Task taskSubmitted2
        Task.withNewTransaction {
            taskSubmitted2 = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the task can't be saved because a task with the same taskId already exists for the same workflow"
        taskSubmitted2.hasErrors()
        taskSubmitted2.errors.getFieldError('taskId').code == 'unique'
        Task.count() == 1
    }

    void "try to submit a task without taskId"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace without taskId"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)
        taskSubmittedTraceJson.task.taskId = null

        when: "unmarshall the JSON to a task"
        Task taskSubmitted
        Task.withNewTransaction {
            taskSubmitted = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the task has a validation error"
        taskSubmitted.hasErrors()
        taskSubmitted.errors.getFieldError('taskId').code == 'nullable'
        Task.count() == 0
    }

    void "try to start a given task without a previous submit trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task started trace"
        Map taskStartedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.STARTED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted1
        Task.withNewTransaction {
            taskSubmitted1 = taskService.processTaskJsonTrace(taskStartedTraceJson)
        }

        then: "the task doesn't exist"
        thrown(NonExistingTaskException)
        Task.count() == 0
    }

    void "receive a submitted trace for a non existing workflow"() {
        given: "a task submitted trace"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, null, TaskStatus.SUBMITTED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted
        Task.withNewTransaction {
            taskSubmitted = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the workflow doesn't exist"
        thrown(NonExistingWorkflowException)
        Task.count() == 0
    }

}

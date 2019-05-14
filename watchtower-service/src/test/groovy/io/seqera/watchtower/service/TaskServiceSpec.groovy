package io.seqera.watchtower.service


import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.controller.TraceWorkflowRequest
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
        TraceWorkflowRequest taskTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        when: "unmarshall the JSON to a task"
        Task task
        Task.withNewTransaction {
            task = taskService.processTaskJsonTrace(taskTraceJson)
        }

        then: "the task has been correctly saved"
        task.id
        task.checkIsSubmitted()
        task.submit
        !task.start
        !task.complete
        Task.count() == 1
    }

    void "submit a task given a submit trace, then start the task given a start trace, last complete the task given a success trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        TraceWorkflowRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        and: 'a task started trace'
        TraceWorkflowRequest taskStartedTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.STARTED)

        and: 'a task succeeded trace'
        TraceWorkflowRequest taskSucceededTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUCCEEDED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted
        Task.withNewTransaction {
            taskSubmitted = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the workflow has been correctly saved"
        taskSubmitted.id
        taskSubmitted.checkIsSubmitted()
        taskSubmitted.submit
        !taskSubmitted.start
        !taskSubmitted.complete
        Task.count() == 1

        when: "unmarshall the started task trace"
        Task taskStarted
        Task.withNewTransaction {
            taskStarted = taskService.processTaskJsonTrace(taskStartedTrace)
        }

        then: "the task has been started"
        taskSubmitted.id == taskStarted.id
        taskStarted.checkIsRunning()
        taskStarted.submit
        taskStarted.start
        !taskStarted.complete
        Task.count() == 1

        when: "unmarshall the succeeded task trace"
        Task taskCompleted
        Task.withNewTransaction {
            taskCompleted = taskService.processTaskJsonTrace(taskSucceededTraceJson)
        }

        then: "the task has been started"
        taskSubmitted.id == taskCompleted.id
        taskStarted.checkIsSucceeded()
        taskStarted.submit
        taskStarted.start
        taskStarted.complete
        Task.count() == 1

        and: "the trace has progress info"
        taskSucceededTraceJson.progress.running == 3
        taskSucceededTraceJson.progress.submitted == 0
        taskSucceededTraceJson.progress.failed == 0
        taskSucceededTraceJson.progress.pending == 0
        taskSucceededTraceJson.progress.succeeded == 1
        taskSucceededTraceJson.progress.cached == 0
    }

    void "submit a task given a submit trace, then start the task given a start trace, last complete the task given a fail trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        TraceWorkflowRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        and: 'a task started trace'
        TraceWorkflowRequest taskStartedTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.STARTED)

        and: 'a task succeeded trace'
        TraceWorkflowRequest taskFailedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.FAILED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted
        Task.withNewTransaction {
            taskSubmitted = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the workflow has been correctly saved"
        taskSubmitted.id
        taskSubmitted.checkIsSubmitted()
        taskSubmitted.submit
        !taskSubmitted.start
        !taskSubmitted.complete
        Task.count() == 1

        when: "unmarshall the started task trace"
        Task taskStarted
        Task.withNewTransaction {
            taskStarted = taskService.processTaskJsonTrace(taskStartedTrace)
        }

        then: "the task has been started"
        taskSubmitted.id == taskStarted.id
        Task.count()

        taskStarted.checkIsRunning()
        taskStarted.submit
        taskStarted.start
        !taskStarted.complete

        when: "unmarshall the succeeded task trace"
        Task taskCompleted
        Task.withNewTransaction {
            taskCompleted = taskService.processTaskJsonTrace(taskFailedTraceJson)
        }

        then: "the task has been started"
        taskSubmitted.id == taskCompleted.id
        taskStarted.checkIsFailed()
        taskStarted.submit
        taskStarted.start
        taskStarted.complete
        taskStarted.errorAction
        Task.count() == 1
    }

    void "submit a task given a submit trace, then try to submit the same one"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        TraceWorkflowRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted1
        Task.withNewTransaction {
            taskSubmitted1 = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the task has been correctly saved"
        taskSubmitted1.id
        taskSubmitted1.checkIsSubmitted()
        taskSubmitted1.submit
        Task.count() == 1

        when: "unmarshall the submit JSON to a second task"
        taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)
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
        TraceWorkflowRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)
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

    void "try to start a task not previously submitted given start trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task started trace"
        TraceWorkflowRequest taskStartedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.STARTED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted1
        Task.withNewTransaction {
            taskSubmitted1 = taskService.processTaskJsonTrace(taskStartedTraceJson)
        }

        then: "the task doesn't exist"
        thrown(NonExistingTaskException)
        Task.count() == 0
    }

    void "try to submit a task given a submit trace for a non existing workflow"() {
        given: "a task submitted trace"
        TraceWorkflowRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, null, TaskStatus.SUBMITTED)

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

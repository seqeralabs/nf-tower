package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.exceptions.NonExistingTaskException
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator
import io.seqera.watchtower.util.TaskTraceSnapshotStatus
import io.seqera.watchtower.util.TracesJsonBank

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class TaskServiceTest extends AbstractContainerBaseTest {

    @Inject
    TaskService taskService


    void "submit a task given a submit trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task JSON submitted trace"
        TraceTaskRequest taskTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)

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

        and: "the workflow progress info was updated"
        task.workflow.progress.running == 0
        task.workflow.progress.submitted == 1
        task.workflow.progress.failed == 0
        task.workflow.progress.pending == 3
        task.workflow.progress.succeeded == 0
        task.workflow.progress.cached == 0
    }

    void "submit a task given a submit trace, then start the task given a start trace, last complete the task given a success trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        TraceTaskRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)

        and: 'a task started trace'
        TraceTaskRequest taskStartedTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.RUNNING)

        and: 'a task succeeded trace'
        TraceTaskRequest taskSucceededTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.SUCCEEDED)

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
        taskStarted.id == taskSubmitted.id
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
        taskCompleted.id == taskSubmitted.id
        taskCompleted.checkIsSucceeded()
        taskCompleted.submit
        taskCompleted.start
        taskCompleted.complete
        Task.count() == 1

        and: "the workflow progress info was updated"
        taskCompleted.workflow.progress.running == 3
        taskCompleted.workflow.progress.submitted == 0
        taskCompleted.workflow.progress.failed == 0
        taskCompleted.workflow.progress.pending == 0
        taskCompleted.workflow.progress.succeeded == 1
        taskCompleted.workflow.progress.cached == 0
    }

    void "submit a task given a submit trace, then start the task given a start trace, last complete the task given a fail trace"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        TraceTaskRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)

        and: 'a task started trace'
        TraceTaskRequest taskStartedTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.RUNNING)

        and: 'a task succeeded trace'
        TraceTaskRequest taskFailedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.FAILED)

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
        taskStarted.id == taskSubmitted.id
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
        taskCompleted.id == taskSubmitted.id
        taskCompleted.checkIsFailed()
        taskCompleted.submit
        taskCompleted.start
        taskCompleted.complete
        taskCompleted.errorAction
        Task.count() == 1

        and: "the workflow progress info was updated"
        taskCompleted.workflow.progress.running == 3
        taskCompleted.workflow.progress.submitted == 0
        taskCompleted.workflow.progress.failed == 0
        taskCompleted.workflow.progress.pending == 0
        taskCompleted.workflow.progress.succeeded == 1
        taskCompleted.workflow.progress.cached == 0
    }

    void "submit a task given a submit trace, then try to submit the same one"() {
        given: 'create the workflow for the task'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: "a task submitted trace"
        TraceTaskRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)

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
        taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)
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
        TraceTaskRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)
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
        TraceTaskRequest taskStartedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.RUNNING)

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
        TraceTaskRequest taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, null, TaskTraceSnapshotStatus.SUBMITTED)

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

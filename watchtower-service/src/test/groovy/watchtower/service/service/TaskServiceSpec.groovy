package watchtower.service.service

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import watchtower.service.domain.Task
import watchtower.service.pogo.enums.TaskStatus
import watchtower.service.pogo.exceptions.NonExistingTaskException
import watchtower.service.pogo.exceptions.NonExistingWorkflowException
import watchtower.service.util.DomainCreator
import watchtower.service.util.TracesJsonBank

@Testcontainers
@MicronautTest(packages = 'watchtower.service.domain')
class TaskServiceSpec extends Specification {

    @Shared
    FixedHostPortGenericContainer mongoDbContainer = new FixedHostPortGenericContainer("mongo:4.1")
            .withFixedExposedPort(27018, 27017)
            .waitingFor(Wait.forHttp('/'))

    @Shared @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    TaskService taskService = embeddedServer.applicationContext.getBean(TaskService)

    void cleanup() {
        DomainCreator.cleanupDatabase()
    }


    void "submit a task given a submit trace"() {
        given: "a task JSON submitted trace"
        Map taskTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.SUBMITTED)

        and: 'create the workflow for the task'
        new DomainCreator().createWorkflow(runId: taskTraceJson.runId, runName: taskTraceJson.runName)

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
    }

    void "submit a task given a submit trace, then start the task given a start trace, last complete the task given a success trace"() {
        given: "a task submitted trace"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.SUBMITTED)

        and: 'a task started trace'
        Map taskStartedTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.STARTED)

        and: 'a task succeeded trace'
        Map taskSucceededTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.SUCCEEDED)

        and: 'create the workflow for the task'
        new DomainCreator().createWorkflow(runId: taskSubmittedTraceJson.runId, runName: taskSubmittedTraceJson.runName)

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
    }

    void "submit a task given a submit trace, then start the task given a start trace, last complete the task given a fail trace"() {
        given: "a task submitted trace"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.SUBMITTED)

        and: 'a task started trace'
        Map taskStartedTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.STARTED)

        and: 'a task succeeded trace'
        Map taskFailedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.FAILED)

        and: 'create the workflow for the task'
        new DomainCreator().createWorkflow(runId: taskSubmittedTraceJson.runId, runName: taskSubmittedTraceJson.runName)

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
        taskStarted.error_action
    }

    void "submit a task given a submit trace, then try to submit the same one"() {
        given: "a task submitted trace"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.SUBMITTED)

        and: 'create the workflow for the task'
        new DomainCreator().createWorkflow(runId: taskSubmittedTraceJson.runId, runName: taskSubmittedTraceJson.runName)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted1
        Task.withNewTransaction {
            taskSubmitted1 = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the task has been correctly saved"
        taskSubmitted1.id
        taskSubmitted1.currentStatus == TaskStatus.SUBMITTED
        taskSubmitted1.submitTime

        when: "unmarshall the submit JSON to a second task"
        Task taskSubmitted2
        Task.withNewTransaction {
            taskSubmitted2 = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the task can't be saved because a task with the same task_id already exists for the same workflow"
        taskSubmitted2.hasErrors()
        taskSubmitted2.errors.getFieldError('task_id')
    }

    void "try to start a task given without a previous submit trace"() {
        given: "a task started trace"
        Map taskStartedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.STARTED)

        and: 'create the workflow for the task'
        new DomainCreator().createWorkflow(runId: taskStartedTraceJson.runId, runName: taskStartedTraceJson.runName)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted1
        Task.withNewTransaction {
            taskSubmitted1 = taskService.processTaskJsonTrace(taskStartedTraceJson)
        }

        then: "the task doesn't exist"
        thrown(NonExistingTaskException)
    }

    void "receive a submitted trace for a non existing workflow"() {
        given: "a task submitted trace"
        Map taskSubmittedTraceJson = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.SUBMITTED)

        when: "unmarshall the JSON to a task"
        Task taskSubmitted
        Task.withNewTransaction {
            taskSubmitted = taskService.processTaskJsonTrace(taskSubmittedTraceJson)
        }

        then: "the workflow has been correctly saved"
        thrown(NonExistingWorkflowException)
    }

}

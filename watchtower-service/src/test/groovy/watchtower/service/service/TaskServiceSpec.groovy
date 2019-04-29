package watchtower.service.service

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Shared
import spock.lang.Specification
import watchtower.service.Application
import watchtower.service.domain.Task
import watchtower.service.domain.Workflow
import watchtower.service.pogo.TaskTraceJsonUnmarshaller
import watchtower.service.pogo.WorkflowTraceJsonUnmarshaller
import watchtower.service.pogo.enums.TaskStatus
import watchtower.service.pogo.enums.WorkflowStatus
import watchtower.service.pogo.exceptions.NonExistingWorkflowException
import watchtower.service.util.DomainCreator
import watchtower.service.util.TracesJsonBank

import java.time.Instant

@MicronautTest(application = Application.class)
class TaskServiceSpec extends Specification {

    @Shared @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    TaskService taskService = embeddedServer.applicationContext.getBean(TaskService)

    void cleanup() {
        DomainCreator.cleanupDatabase()
    }


    void "create a task"() {
        given: 'a task'
        Workflow existingWorkflow = new DomainCreator().createWorkflow(runId: "a36c5f5b-e0bd-4d83-9225-1a6e73930d7b", runName: "hopeful_swartz")

        Task task = new Task(workflow: existingWorkflow)
        TaskTraceJsonUnmarshaller.populateTaskFields(TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.SUBMITTED), TaskStatus.SUBMITTED, task)

        Task.withNewTransaction {
            task.save()
            task
        }
//        Task task = new DomainCreator().createTask([    "task_id": 1,
//                                                        "status": "RUNNING",
//                                                        "hash": "6c/8001bb",
//                                                        "name": "index (ggal_1_48850000_49020000)",
//                                                        "exit": 2147483647,
//                                                        "submit": 1556011733493,
//                                                        "start": 1556011733534,
//                                                        "process": "index",
//                                                        "tag": "ggal_1_48850000_49020000",
//                                                        "module": [],
//                                                        "container": "nextflow/rnaseq-nf@sha256:e221e2511abb89a0cf8c32f6cd9b125fbfeb7f7c386a1f49299f48d7735faacd",
//                                                        "attempt": 1,
//                                                        "script": "\n    salmon index --threads 1 -t ggal_1_48850000_49020000.Ggal71.500bpflank.fa -i index\n    ",
//                                                        "scratch": null,
//                                                        "workdir": "/Users/pditommaso/Projects/nextflow/work/6c/8001bb0bafaa9dd14af01aae92f2be",
//                                                        "queue": null,
//                                                        "cpus": 1,
//                                                        "memory": null,
//                                                        "disk": null,
//                                                        "time": null,
//                                                        "env": null,
//                                                        "native_id": 26849])
        expect:
        task.id
        Task.count()
    }

    void "start a workflow given a started trace"() {
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
        Task.count()

        task.currentStatus == TaskStatus.SUBMITTED
        task.submitTime
        !task.startTime
        !task.completeTime
    }

    void "start a workflow given a started trace, then complete the task given a succeeded trace"() {
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
        Task.count()

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
            taskCompleted = taskService.processTaskJsonTrace(taskSucceededTraceJson)
        }

        then: "the task has been started"
        taskSubmitted.id == taskCompleted.id
        taskStarted.currentStatus == TaskStatus.SUCCEEDED
        taskStarted.submitTime
        taskStarted.startTime
        taskStarted.completeTime
    }

    @Ignore
    void "start a workflow given a started trace, then complete the workflow given a failed trace"() {
        given: "a workflow JSON started trace"
        Map workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, WorkflowStatus.STARTED)

        and: 'a workflow completed trace'
        Map workflowFailedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, WorkflowStatus.FAILED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processWorkflowJsonTrace(workflowStartedTraceJson)
        }

        then: "the workflow has been correctly saved"
        workflowStarted.id
        workflowStarted.currentStatus == WorkflowStatus.STARTED
        workflowStarted.submitTime
        !workflowStarted.completeTime

        when: "unmarshall the failed JSON to a workflow"
        Workflow workflowFailed
        Workflow.withNewTransaction {
            workflowFailed = workflowService.processWorkflowJsonTrace(workflowFailedTraceJson)
        }

        then: "the workflow has been completed"
        workflowStarted.id == workflowFailed.id
        workflowFailed.currentStatus == WorkflowStatus.FAILED
        workflowFailed.submitTime
        workflowFailed.completeTime
    }

    @Ignore
    void "start a workflow given a started trace, then try to start the same one"() {
        given: "a workflow JSON started trace"
        Map workflowStarted1TraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, WorkflowStatus.STARTED)

        and: 'a workflow completed trace'
        Map workflowStarted2TraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted1
        Workflow.withNewTransaction {
            workflowStarted1 = workflowService.processWorkflowJsonTrace(workflowStarted1TraceJson)
        }

        then: "the workflow has been correctly saved"
        workflowStarted1.id
        workflowStarted1.currentStatus == WorkflowStatus.STARTED
        workflowStarted1.submitTime
        !workflowStarted1.completeTime

        when: "unmarshall the started JSON to a second workflow"
        Workflow workflowStarted2
        Workflow.withNewTransaction {
            workflowStarted2 = workflowService.processWorkflowJsonTrace(workflowStarted2TraceJson)
        }

        then: "the workflow can't be saved because a workflow with the same runId and runName already exists"
        workflowStarted2.hasErrors()
        workflowStarted2.errors.getFieldError('runId')
    }

    @Ignore
    void "receive a succeeded trace without receiving a previous started trace"() {
        given: "a workflow JSON started trace"
        Map workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, WorkflowStatus.SUCCEEDED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowSucceeded
        Workflow.withNewTransaction {
            workflowSucceeded = workflowService.processWorkflowJsonTrace(workflowSucceededTraceJson)
        }

        then: "the workflow has been correctly saved"
        thrown(NonExistingWorkflowException)
    }

}

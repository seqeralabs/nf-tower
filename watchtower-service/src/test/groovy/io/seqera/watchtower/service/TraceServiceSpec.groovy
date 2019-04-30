package io.seqera.watchtower.service

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TraceType
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.DomainCreator
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.util.mop.ConfineMetaClassChanges

@MicronautTest(application = Application.class)
class TraceServiceSpec extends AbstractContainerBaseSpec {

    @Shared @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    TraceService traceService = embeddedServer.applicationContext.getBean(TraceService)


    @ConfineMetaClassChanges([WorkflowService])
    void "process a successful workflow trace"() {
        given: "mock the workflow JSON processor to return a successful workflow"
        Workflow workflow = new DomainCreator().createWorkflow()
        traceService.workflowService.metaClass.processWorkflowJsonTrace { Map workflowJson ->
            workflow
        }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates a successful processing"
        result.traceType == TraceType.WORKFLOW
        result.entityId
        !result.error
    }

    @ConfineMetaClassChanges([WorkflowService])
    void "process a workflow trace to start a new workflow with the same runId+runName combination of a previous one"() {
        given: "mock the workflow JSON processor to return a workflow with the same runId+runName combination as a previous one"
        Workflow workflow1 = new DomainCreator().createWorkflow()
        Workflow workflow2 = new DomainCreator(failOnError: false).createWorkflow(runId: workflow1.runId, runName: workflow1.runName)
        traceService.workflowService.metaClass.processWorkflowJsonTrace { Map workflowJson ->
            workflow2
        }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        result.traceType == TraceType.WORKFLOW
        result.error == "Can't start an existing workflow"
        !result.entityId
    }

    @ConfineMetaClassChanges([WorkflowService])
    void "process a workflow trace to start workflow without submitTime"() {
        given: "mock the workflow JSON processor to return a workflow with the same runId+runName combination as a previous one"
        Workflow workflow = new DomainCreator(failOnError: false).createWorkflow(submitTime: null)
        traceService.workflowService.metaClass.processWorkflowJsonTrace { Map workflowJson ->
            workflow
        }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        result.traceType == TraceType.WORKFLOW
        result.error == "Can't complete a non-existing workflow"
        !result.entityId
    }

    @ConfineMetaClassChanges([WorkflowService])
    void "process a workflow trace, but throw a NonExistingWorkflow exception"() {
        given: "mock the workflow JSON processor to throw an exception"
        String exceptionMessage = 'message'
        traceService.workflowService.metaClass.processWorkflowJsonTrace { Map workflowJson ->
            throw(new NonExistingWorkflowException(exceptionMessage))
        }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        result.traceType == TraceType.WORKFLOW
        result.error == exceptionMessage
        !result.entityId
    }

    @ConfineMetaClassChanges([WorkflowService])
    void "process a workflow trace, but throw a generic exception"() {
        given: "mock the workflow JSON processor to throw an exception"
        traceService.workflowService.metaClass.processWorkflowJsonTrace { Map workflowJson ->
            throw(new RuntimeException())
        }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        result.traceType == TraceType.WORKFLOW
        result.error == "Can't process JSON: check format"
        !result.entityId
    }

    @ConfineMetaClassChanges([TaskService])
    void "process a successful task trace"() {
        given: "mock the task JSON processor to return a successful task"
        Task task = new DomainCreator().createTask()
        traceService.taskService.metaClass.processTaskJsonTrace { Map taskJson ->
            task
        }

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        result.traceType == TraceType.TASK
        result.entityId
        !result.error
    }

    @ConfineMetaClassChanges([TaskService])
    void "process a task without submit time"() {
        given: "mock the task JSON processor to return a task without submit time"
        Task task = new DomainCreator(failOnError: false).createTask(submitTime: null)
        traceService.taskService.metaClass.processTaskJsonTrace { Map taskJson ->
            task
        }

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        result.traceType == TraceType.TASK
        !result.entityId
        result.error == "Can't start or complete a non-existing task"
    }

    @ConfineMetaClassChanges([TaskService])
    void "process a task with the same task_id of a previous one for the same workflow"() {
        given: "mock the task JSON processor to return a task with the same task_id of a previous one for the same workflow"
        Workflow workflow = new DomainCreator().createWorkflow()
        Task task1 = new DomainCreator().createTask(workflow: workflow)
        Task task2 = new DomainCreator(failOnError: false).createTask(workflow: workflow, task_id: task1.task_id)
        traceService.taskService.metaClass.processTaskJsonTrace { Map taskJson ->
            task2
        }

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        result.traceType == TraceType.TASK
        !result.entityId
        result.error == "Can't submit a task which was already submitted"
    }


}

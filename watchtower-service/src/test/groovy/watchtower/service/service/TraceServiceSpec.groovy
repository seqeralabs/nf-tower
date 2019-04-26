package watchtower.service.service

import io.micronaut.context.ApplicationContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import watchtower.service.Application
import watchtower.service.domain.Workflow
import watchtower.service.pogo.enums.TraceType
import watchtower.service.pogo.exceptions.WorkflowNotExistsException
import watchtower.service.util.DomainCreator

@MicronautTest(application = Application.class)
class TraceServiceSpec extends Specification {

    @Shared @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    TraceService traceService = embeddedServer.applicationContext.getBean(TraceService)


    void cleanup() {
        DomainCreator.cleanupDatabase()
    }


    @ConfineMetaClassChanges([WorkflowService])
    void "process a successful workflow trace"() {
        given: "mock the workflow JSON processor to return a successful workflow"
        Workflow workflow = new DomainCreator(withNewTransaction: true).createWorkflow()
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
        Workflow workflow1 = new DomainCreator(withNewTransaction: true).createWorkflow()
        Workflow workflow2 = new DomainCreator(withNewTransaction: true, failOnError: false).createWorkflow(runId: workflow1.runId, runName: workflow1.runName)
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
        Workflow workflow = new DomainCreator(withNewTransaction: true, failOnError: false).createWorkflow(submitTime: null)
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
            throw(new WorkflowNotExistsException(exceptionMessage))
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


}

package io.seqera.watchtower.service

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TraceType
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
class TraceServiceSpec extends AbstractContainerBaseSpec {

    @Inject
    WorkflowService workflowService
    @MockBean(WorkflowServiceImpl)
    WorkflowService workflowService() {
        Mock(WorkflowService)
    }

    @Inject
    TaskService taskService
    @MockBean(TaskServiceImpl)
    TaskService taskService() {
        Mock(TaskService)
    }

    @Inject
    TraceService traceService


    void "process a successful workflow trace"() {
        given: "mock the workflow JSON processor to return a successful workflow"
        Workflow workflow = new DomainCreator().createWorkflow()
        workflowService.processWorkflowJsonTrace(_) >> workflow

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates a successful processing"
        result.traceType == TraceType.WORKFLOW
        result.entityId
        !result.error
    }

    void "process a workflow trace to start a new workflow with the same sessionId+runName combination of a previous one"() {
        given: "mock the workflow JSON processor to return a workflow with the same sessionId+runName combination as a previous one"
        Workflow workflow1 = new DomainCreator().createWorkflow()
        Workflow workflow2 = new DomainCreator(failOnError: false).createWorkflow(sessionId: workflow1.sessionId, runName: workflow1.runName)
        workflowService.processWorkflowJsonTrace(_) >> workflow2

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        result.traceType == TraceType.WORKFLOW
        result.error == "Can't start an existing workflow"
        !result.entityId
    }

    void "process a workflow trace to start workflow without submitTime"() {
        given: "mock the workflow JSON processor to return a workflow without submitTime"
        Workflow workflow = new DomainCreator(failOnError: false).createWorkflow(submitTime: null)
        workflowService.processWorkflowJsonTrace(_) >> workflow

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        result.traceType == TraceType.WORKFLOW
        result.error == "Can't complete a non-existing workflow"
        !result.entityId
    }

    void "process a workflow trace, but throw a NonExistingWorkflow exception"() {
        given: "mock the workflow JSON processor to throw an exception"
        String exceptionMessage = 'message'
        workflowService.processWorkflowJsonTrace(_) >> { throw(new NonExistingWorkflowException(exceptionMessage)) }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        result.traceType == TraceType.WORKFLOW
        result.error == exceptionMessage
        !result.entityId
    }

    void "process a workflow trace, but throw a generic exception"() {
        given: "mock the workflow JSON processor to throw an exception"
        workflowService.processWorkflowJsonTrace(_) >> { throw(new RuntimeException()) }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        result.traceType == TraceType.WORKFLOW
        result.error == "Can't process JSON: check format"
        !result.entityId
    }

    void "process a successful task trace"() {
        given: "mock the task JSON processor to return a successful task"
        Task task = new DomainCreator().createTask()
        taskService.processTaskJsonTrace(_) >> task

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        result.traceType == TraceType.TASK
        result.entityId
        !result.error
    }

    void "process a task without submit time"() {
        given: "mock the task JSON processor to return a task without submit time"
        Task task = new DomainCreator(failOnError: false).createTask(submitTime: null)
        taskService.processTaskJsonTrace(_) >> task

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        result.traceType == TraceType.TASK
        !result.entityId
        result.error == "Can't start or complete a non-existing task"
    }

    void "process a task with the same taskId of a previous one for the same workflow"() {
        given: "mock the task JSON processor to return a task with the same taskId of a previous one for the same workflow"
        Workflow workflow = new DomainCreator().createWorkflow()
        Task task1 = new DomainCreator().createTask(workflow: workflow)
        Task task2 = new DomainCreator(failOnError: false).createTask(workflow: workflow, taskId: task1.taskId)
        taskService.processTaskJsonTrace(_) >> task2

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        Map result = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        result.traceType == TraceType.TASK
        !result.entityId
        result.error == "Can't submit a task which was already submitted"
    }

}

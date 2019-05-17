package io.seqera.watchtower.service

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse
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
        TraceWorkflowResponse response = traceService.processWorkflowTrace(null)

        then: "the result indicates a successful processing"
        response.workflowId
        !response.message
    }

    void "process a workflow trace to try to start a new workflow with the same sessionId+runName combination of a previous one"() {
        given: "mock the workflow JSON processor to return a workflow with the same sessionId+runName combination as a previous one"
        Workflow workflow1 = new DomainCreator().createWorkflow()
        Workflow workflow2 = new DomainCreator(failOnError: false).createWorkflow(sessionId: workflow1.sessionId, runName: workflow1.runName)
        workflowService.processWorkflowJsonTrace(_) >> workflow2

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        TraceWorkflowResponse response = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        response.message == "Can't save a workflow with the same sessionId of another"
        !response.workflowId
    }

    void "process a workflow trace to try to start workflow without submitTime"() {
        given: "mock the workflow JSON processor to return a workflow without submitTime"
        Workflow workflow = new DomainCreator(failOnError: false).createWorkflow(submit: null)
        workflowService.processWorkflowJsonTrace(_) >> workflow

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        TraceWorkflowResponse response = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        response.message.startsWith("Can't save a workflow without") && (response.message.endsWith("start") || response.message.endsWith("submit"))
        !response.workflowId
    }

    void "process a workflow trace, but throw a NonExistingWorkflow exception"() {
        given: "mock the workflow JSON processor to throw an exception"
        String exceptionMessage = 'message'
        workflowService.processWorkflowJsonTrace(_) >> { throw(new NonExistingWorkflowException(exceptionMessage)) }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        TraceWorkflowResponse response = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        response.message == exceptionMessage
        !response.workflowId
    }

    void "process a workflow trace, but throw a generic exception"() {
        given: "mock the workflow JSON processor to throw an exception"
        workflowService.processWorkflowJsonTrace(_) >> { throw(new RuntimeException()) }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        TraceWorkflowResponse response = traceService.processWorkflowTrace(null)

        then: "the result indicates an error"
        response.message == "Can't process JSON: check format"
        !response.workflowId
    }

    void "process a successful task trace"() {
        given: "mock the task JSON processor to return a successful task"
        Task task = new DomainCreator().createTask()
        taskService.processTaskJsonTrace(_) >> task

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        TraceTaskResponse response = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        response.workflowId
        !response.message
    }

    void "process a task trace to try to submit a task without submit time"() {
        given: "mock the task JSON processor to return a task without submit time"
        Task task = new DomainCreator(failOnError: false).createTask(submit: null)
        taskService.processTaskJsonTrace(_) >> task

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        TraceTaskResponse response = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        !response.workflowId
        response.message == "Can't save a task without submit"
    }

    void "process a task task trace to try to submit a task without taskId"() {
        given: "mock the task JSON processor to return a task without taskId"
        Workflow workflow = new DomainCreator().createWorkflow()
        Task task = new DomainCreator(failOnError: false).createTask(workflow: workflow, taskId: null)
        taskService.processTaskJsonTrace(_) >> task

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        TraceTaskResponse response = traceService.processTaskTrace(null)

        then: "the result indicates a failed processing"
        !response.workflowId
        response.message == "Can't save a task without taskId"
    }

    void "process a task trace to try to submit a task with the same taskId of a previous one for the same workflow"() {
        given: "mock the task JSON processor to return a task with the same taskId of a previous one for the same workflow"
        Workflow workflow = new DomainCreator().createWorkflow()
        Task task1 = new DomainCreator().createTask(workflow: workflow)
        Task task2 = new DomainCreator(failOnError: false).createTask(workflow: workflow, taskId: task1.taskId)
        taskService.processTaskJsonTrace(_) >> task2

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        TraceTaskResponse response = traceService.processTaskTrace(null)

        then: "the result indicates a successful processing"
        !response.workflowId
        response.message == "Can't save a task with the same taskId of another"
    }

}

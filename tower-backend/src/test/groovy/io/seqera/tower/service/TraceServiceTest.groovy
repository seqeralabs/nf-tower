/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.service

import javax.inject.Inject
import javax.validation.ValidationException

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.seqera.tower.Application
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.exceptions.NonExistingWorkflowException
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.service.progress.ProgressService
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

@MicronautTest(application = Application.class)
class TraceServiceTest extends AbstractContainerBaseTest {

    @Inject
    WorkflowService workflowService

    @MockBean(WorkflowServiceImpl)
    WorkflowService workflowService() {
        Mock(WorkflowService)
    }

    @Inject
    TaskService taskService

    @Inject
    ProgressService progressService
    
    @MockBean(TaskServiceImpl)
    TaskService taskService() { Mock(TaskService) }

    @Inject
    TraceService traceService


    void "process a successful workflow trace"() {
        given: "mock the workflow JSON processor to return a successful workflow"
        def req = new TraceWorkflowRequest(processNames: [])
        Workflow workflow = new DomainCreator().createWorkflow()
        workflowService.processTraceWorkflowRequest(_, _) >> workflow

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        Workflow processedWorkflow = traceService.processWorkflowTrace(req, null)

        then: "the result indicates a successful processing"
        processedWorkflow.id
        !processedWorkflow.hasErrors()
    }

    void "process a workflow trace to try to start a new workflow with the same sessionId+runName combination of a previous one"() {
        given: "mock the workflow JSON processor to return a workflow with the same sessionId+runName combination as a previous one"
        def req = new TraceWorkflowRequest(processNames: [])
        Workflow workflow1 = new DomainCreator().createWorkflow()
        Workflow workflow2 = new DomainCreator(failOnError: false).createWorkflow(sessionId: workflow1.sessionId, runName: workflow1.runName)
        workflowService.processTraceWorkflowRequest(_, _) >> workflow2

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        traceService.processWorkflowTrace(req, null)

        then: "the result indicates an error"
        Exception e = thrown(ValidationException)
        e.message == "Can't save a workflow with the same runName of another"
    }

    void "process a workflow trace to try to start workflow without submitTime"() {
        given: "mock the workflow JSON processor to return a workflow without submitTime"
        def req = new TraceWorkflowRequest(processNames: [])
        Workflow workflow = new DomainCreator(failOnError: false).createWorkflow(submit: null)
        workflowService.processTraceWorkflowRequest(_, _) >> workflow

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        traceService.processWorkflowTrace(req, null)

        then: "the result indicates an error"
        Exception e = thrown(ValidationException)
        e.message.startsWith("Can't save a workflow without") && (e.message.endsWith("start") || e.message.endsWith("submit"))
    }

    void "process a workflow trace, but throw a NonExistingWorkflow exception"() {
        given: "mock the workflow JSON processor to throw an exception"
        String exceptionMessage = 'message'
        workflowService.processTraceWorkflowRequest(_, _) >> { throw(new NonExistingWorkflowException(exceptionMessage)) }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        traceService.processWorkflowTrace(null, null)

        then: "the result indicates an error"
        Exception e = thrown(NonExistingWorkflowException)
        e.message == exceptionMessage
    }

    void "process a workflow trace, but throw a generic exception"() {
        given: "mock the workflow JSON processor to throw an exception"
        String exceptionMessage = 'message'
        workflowService.processTraceWorkflowRequest(_, _) >> { throw(new RuntimeException(exceptionMessage)) }

        when: "process the workflow (we don't mind about the given JSON because the processor is mocked)"
        traceService.processWorkflowTrace(null, null)

        then: "the result indicates an error"
        Exception e = thrown(RuntimeException)
        e.message == exceptionMessage
    }

    void "process a successful task trace"() {
        given: "mock the task JSON processor to return a successful task"
        def req = new TraceTaskRequest(workflowId: 'xyz')
        Task task = new DomainCreator().createTask()
        taskService.processTaskTraceRequest(_) >> [task]
        progressService.create('xyz', [])
        and:

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        def processedTask = traceService.processTaskTrace(req).first()

        then: "the result indicates a successful processing"
        processedTask.workflowId
    }

    void "process a task task trace to try to submit a task without taskId"() {
        given: "mock the task JSON processor to return a task without taskId"
        def request = new TraceTaskRequest(workflowId: 'xyz')
        Workflow workflow = new DomainCreator().createWorkflow()
        Task task = new DomainCreator(failOnError: false).createTask(workflow: workflow, taskId: null)
        taskService.processTaskTraceRequest(_) >> [task]

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        traceService.processTaskTrace(request)

        then: "the result indicates a failed processing"
        Exception e = thrown(ValidationException)
        e.message == "Can't save a task without taskId"
    }

    void "process a task trace to try to submit a task with the same taskId of a previous one for the same workflow"() {
        given: "mock the task JSON processor to return a task with the same taskId of a previous one for the same workflow"
        def request = new TraceTaskRequest(workflowId: 'xyz')
        Workflow workflow = new DomainCreator().createWorkflow()
        Task task1 = new DomainCreator().createTask(workflow: workflow)
        Task task2 = new DomainCreator(failOnError: false).createTask(workflow: workflow, taskId: task1.taskId)
        taskService.processTaskTraceRequest(_) >> [task2]

        when: "process the task (we don't mind about the given JSON because the processor is mocked)"
        traceService.processTaskTrace(request)

        then: "the result indicates a successful processing"
        Exception e = thrown(ValidationException)
        e.message == "Can't save a task with the same taskId of another"
    }

}

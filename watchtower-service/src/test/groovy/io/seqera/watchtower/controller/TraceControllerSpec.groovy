package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.enums.WorkflowStatus
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.DomainCreator
import io.seqera.watchtower.util.NextflowSimulator
import io.seqera.watchtower.util.TracesJsonBank
import spock.lang.Ignore

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class TraceControllerSpec extends AbstractContainerBaseSpec {

    @Inject
    @Client('/')
    RxHttpClient client


    void "save a new workflow given a start trace"() {
        given: 'a workflow started JSON trace'
        TraceWorkflowRequest workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: 'send a save request'
        HttpResponse<TraceWorkflowResponse> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/workflow', workflowStartedJsonTrace),
                TraceWorkflowResponse.class
        )

        then: 'the workflow has been saved successfully'
        response.status == HttpStatus.CREATED
        response.body().workflowId

        and: 'the workflow is in the database'
        Workflow.count() == 1
    }

    void "save a new task given a submit trace"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a task submitted JSON trace'
        TraceTaskRequest taskSubmittedJsonTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        when: 'send a save request'
        HttpResponse<TraceTaskResponse> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/task', taskSubmittedJsonTrace),
                TraceTaskResponse.class
        )

        then: 'the task has been saved successfully'
        response.status == HttpStatus.CREATED
        response.body().workflowId

        and: 'the task is in the database'
        Task.count() == 1
    }

    @Ignore("throws 'IllegalStateException: state should be: open' when executing all tests")
    void "save traces simulated from a complete sequence"() {
        given: 'a nextflow simulator'
        NextflowSimulator nextflowSimulator = new NextflowSimulator(workflowOrder: 2, client: client.toBlocking(), sleepBetweenRequests: 0)

        when: 'simulate nextflow'
        nextflowSimulator.simulate()

        then: 'the workflow and its tasks have been saved'
        Workflow.count() == 1
        Task.count() == 2
    }

}

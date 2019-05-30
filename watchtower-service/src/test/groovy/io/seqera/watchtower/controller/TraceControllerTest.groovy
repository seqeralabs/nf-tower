package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.subscribers.TestSubscriber
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.SseErrorType
import io.seqera.watchtower.pogo.enums.TraceProcessingStatus
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse
import io.seqera.watchtower.pogo.exchange.trace.sse.TraceSseResponse
import io.seqera.watchtower.util.*
import spock.lang.Ignore

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
@Ignore("throws 'IllegalStateException: state should be: open' when executing all tests")
class TraceControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    @Client('/')
    DefaultHttpClient sseClient


    void "save a new workflow given a start trace"() {
        given: 'a workflow started JSON trace'
        TraceWorkflowRequest workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowTraceSnapshotStatus.STARTED)

        when: 'send a save request'
        HttpResponse<TraceWorkflowResponse> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/workflow', workflowStartedJsonTrace),
                TraceWorkflowResponse.class
        )

        then: 'the workflow has been saved successfully'
        response.status == HttpStatus.CREATED
        response.body().status == TraceProcessingStatus.OK
        response.body().workflowId
        !response.body().message

        and: 'the workflow is in the database'
        Workflow.count() == 1
    }

    void "save a new task given a submit trace"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a task submitted JSON trace'
        TraceTaskRequest taskSubmittedJsonTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)

        when: 'send a save request'
        HttpResponse<TraceTaskResponse> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/task', taskSubmittedJsonTrace),
                TraceTaskResponse.class
        )

        then: 'the task has been saved successfully'
        response.status == HttpStatus.CREATED
        response.body().status == TraceProcessingStatus.OK
        response.body().workflowId
        !response.body().message

        and: 'the task is in the database'
        Task.count() == 1
    }

    @Ignore
    void "get the trace update SSE live events"() {
        given: 'save a workflow started JSON trace'
        TraceWorkflowRequest workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowTraceSnapshotStatus.STARTED)

        when: 'send a save request'
        HttpResponse<TraceWorkflowResponse> responseWorkflow = client.toBlocking().exchange(
                HttpRequest.POST('/trace/workflow', workflowStartedJsonTrace),
                TraceWorkflowResponse.class
        )

        then: 'the workflow has been saved successfully'
        responseWorkflow.status == HttpStatus.CREATED
        responseWorkflow.body().status == TraceProcessingStatus.OK
        def workflowId = responseWorkflow.body().workflowId
        !responseWorkflow.body().message

        when: 'subscribe to the live events endpoint'
        TestSubscriber subscriber = new TestSubscriber()
        sseClient.eventStream("/trace/live/${workflowId}", TraceSseResponse.class)
                 .subscribe(subscriber)

        then: 'the flowable is active'
        subscriber.assertNotComplete()

        and: 'save a task submitted JSON trace'
        TraceTaskRequest taskSubmittedJsonTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflowId as Long, TaskTraceSnapshotStatus.SUBMITTED)

        when: 'send a save request'
        HttpResponse<TraceTaskResponse> responseTask = client.toBlocking().exchange(
                HttpRequest.POST('/trace/task', taskSubmittedJsonTrace),
                TraceTaskResponse.class
        )

        then: 'the task has been saved successfully'
        responseTask.status == HttpStatus.CREATED
        responseTask.body().status == TraceProcessingStatus.OK
        responseTask.body().workflowId
        !responseTask.body().message

        and: 'an event has been sent'
        sleep(1000) // <-- sleep a prudential time in order to make sure the event has been received
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.task
        subscriber.events.first()[0].data.task.task
        subscriber.events.first()[0].data.task.progress
    }

    void "save traces simulated from a complete sequence"() {
        given: 'a nextflow simulator'
        NextflowSimulator nextflowSimulator = new NextflowSimulator(workflowOrder: 2, client: client.toBlocking(), sleepBetweenRequests: 0)

        when: 'simulate nextflow'
        nextflowSimulator.simulate()

        then: 'the workflow and its tasks have been saved'
        Workflow.count() == 1
        Task.count() == 2
    }

    void "save traces simulated from a complete sequence and subscribe to the live events in the mean time"() {
        given: 'a nextflow simulator'
        NextflowSimulator nextflowSimulator = new NextflowSimulator(workflowOrder: 2, client: client.toBlocking(), sleepBetweenRequests: 0)

        when: 'send the first request to start the workflow'
        nextflowSimulator.simulate(1)

        then: 'the workflow has been created'
        Workflow.count() == 1

        when: 'subscribe to the live events endpoint'
        TestSubscriber subscriber = new TestSubscriber()
        sseClient.eventStream("/trace/live/${nextflowSimulator.workflowId}", TraceSseResponse.class)
                 .subscribe(subscriber)

        then: 'the flowable is active'
        subscriber.assertNotComplete()

        when: 'keep simulating with the next task request'
        nextflowSimulator.simulate(1)

        then: 'the task has been created'
        Task.count() == 1

        and: 'the task event has been sent'
        sleep(500) // <-- sleep a prudential time in order to make sure the event has been received
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.task
        subscriber.events.first()[0].data.task.task
        subscriber.events.first()[0].data.task.progress

        when: 'keep the simulation going'
        nextflowSimulator.simulate()

        then: 'try to resubscribe to the workflow live updates once completed'
        TraceSseResponse sseResponse = sseClient.eventStream("/trace/live/${nextflowSimulator.workflowId}", TraceSseResponse.class).blockingFirst().data
        sseResponse.error.type == SseErrorType.NONEXISTENT
    }

}

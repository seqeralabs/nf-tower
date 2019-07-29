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

package io.seqera.tower.controller

import javax.inject.Inject

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.subscribers.TestSubscriber
import io.seqera.tower.Application
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.SseErrorType
import io.seqera.tower.enums.TraceProcessingStatus
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceTaskResponse
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.exchange.trace.TraceWorkflowResponse
import io.seqera.tower.exchange.trace.sse.TraceSseResponse
import io.seqera.tower.service.auth.AuthenticationProviderByAccessToken
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import io.seqera.tower.util.NextflowSimulator
import io.seqera.tower.util.TaskTraceSnapshotStatus
import io.seqera.tower.util.TracesJsonBank
import io.seqera.tower.util.WorkflowTraceSnapshotStatus

@MicronautTest(application = Application.class)
@Transactional
class TraceControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    @Client('/')
    DefaultHttpClient sseClient

    protected HttpRequest appendBasicAuth(User user, MutableHttpRequest request) {
        request.basicAuth(AuthenticationProviderByAccessToken.ID, user.accessTokens.first().token)
    }


    void "save a new workflow given a start trace"() {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()

        and: 'a workflow started JSON trace'
        TraceWorkflowRequest workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/workflow', workflowStartedJsonTrace)
        request = appendBasicAuth(user, request)

        HttpResponse<TraceWorkflowResponse> response = client.toBlocking().exchange(
                request,
                TraceWorkflowResponse.class
        )

        then: 'the workflow has been saved successfully'
        response.status == HttpStatus.CREATED
        response.body().status == TraceProcessingStatus.OK
        response.body().workflowId
        !response.body().message

        and: 'the workflow is in the database'
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }

        and: 'the user has been associated with the workflow'
        User.withNewTransaction {
            user.refresh().workflows
        }
    }

    void "save a new task given a submit trace"() {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()

        and: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a task submitted JSON trace'
        TraceTaskRequest taskSubmittedJsonTrace = TracesJsonBank.extractTaskJsonTrace('success', 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/task', taskSubmittedJsonTrace)
        request = appendBasicAuth(user, request)

        HttpResponse<TraceTaskResponse> response = client.toBlocking().exchange(
                request,
                TraceTaskResponse.class
        )

        then: 'the task has been saved successfully'
        response.status == HttpStatus.CREATED
        response.body().status == TraceProcessingStatus.OK
        response.body().workflowId
        !response.body().message

        and: 'the task is in the database'
        Task.withNewTransaction {
            Task.count() == 1
        }
    }

    void "try to save a new workflow without being authenticated"() {
        given: 'a workflow started JSON trace'
        TraceWorkflowRequest workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/workflow', workflowStartedJsonTrace)
        client.toBlocking().exchange(
                request,
                TraceWorkflowResponse.class
        )

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

    void "try to save a new task without being authenticated"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a task submitted JSON trace'
        TraceTaskRequest taskSubmittedJsonTrace = TracesJsonBank.extractTaskJsonTrace('success', 1, workflow.id, TaskTraceSnapshotStatus.SUBMITTED)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/task', taskSubmittedJsonTrace)
        client.toBlocking().exchange(
            request,
            TraceTaskResponse.class
        )

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

    void "save traces simulated from a complete sequence"() {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()

        and: 'a nextflow simulator'
        NextflowSimulator nextflowSimulator = new NextflowSimulator(user: user, workflowLabel: 'simulation', client: client.toBlocking(), sleepBetweenRequests: 0)

        when: 'simulate nextflow'
        nextflowSimulator.simulate()

        then: 'the workflow and its tasks have been saved'
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }
        Workflow.withNewTransaction {
            Task.count() == 15
        }
    }

    void "save traces simulated from a complete sequence and subscribe to the live events in the mean time"() {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()

        and: 'a nextflow simulator'
        NextflowSimulator nextflowSimulator = new NextflowSimulator(user: user, workflowLabel: 'simulation', client: client.toBlocking(), sleepBetweenRequests: 0)

        when: 'subscribe to the live events for the workflow list endpoint'
        TestSubscriber listSubscriber = sseClient.eventStream("/trace/live/workflowList/${user.id}", TraceSseResponse.class).test()

        then: 'the list flowable has just been created (is active)'
        listSubscriber.assertNotComplete()

        and: 'send the first request to start the workflow'
        nextflowSimulator.simulate(1)

        then: 'the workflow has been created'
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }

        when: 'subscribe to the live events for the workflow detail endpoint'
        TestSubscriber detailSubscriber = sseClient.eventStream("/trace/live/workflowDetail/${nextflowSimulator.workflowId}", TraceSseResponse.class).test()

        then: 'the detail flowable is active'
        detailSubscriber.assertNotComplete()

        when: 'keep simulating with the next task request'
        nextflowSimulator.simulate(1)

        then: 'the tasks have been created'
        Task.withNewTransaction {
            Task.count() == 6
        }

        and: 'the task progress event has been sent'
        detailSubscriber.awaitCount(1)
        detailSubscriber.assertValueCount(1)

        when: 'keep the simulation going'
        nextflowSimulator.simulate()

        then: 'try to resubscribe to the workflow live updates once completed'
        TraceSseResponse sseResponse = sseClient.eventStream("/trace/live/workflowDetail/${nextflowSimulator.workflowId}", TraceSseResponse.class).blockingFirst().data
        sseResponse.error.type == SseErrorType.NONEXISTENT
    }

}

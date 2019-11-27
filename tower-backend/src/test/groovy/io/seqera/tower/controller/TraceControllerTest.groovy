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
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.enums.TraceProcessingStatus
import io.seqera.tower.enums.WorkflowStatus
import io.seqera.tower.exchange.trace.TraceAliveRequest
import io.seqera.tower.exchange.trace.TraceAliveResponse
import io.seqera.tower.exchange.trace.TraceInitRequest
import io.seqera.tower.exchange.trace.TraceInitResponse
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceTaskResponse
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.exchange.trace.TraceWorkflowResponse
import io.seqera.tower.service.auth.AuthenticationByApiToken
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import io.seqera.tower.util.NextflowSimulator
import io.seqera.tower.util.TaskTraceSnapshotStatus
import io.seqera.tower.util.TracesJsonBank
import io.seqera.tower.util.WorkflowTraceSnapshotStatus
import spock.lang.Timeout

@Timeout(10)
@MicronautTest(application = Application.class)
@Transactional
class TraceControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client


    protected HttpRequest appendBasicAuth(User user, MutableHttpRequest request) {
        request.basicAuth(AuthenticationByApiToken.ID, user.accessTokens.first().token)
    }

    void 'should response to to hello' () {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/init', new TraceInitRequest(sessionId: 'xyz'))
        request = appendBasicAuth(user, request)

        HttpResponse<TraceInitResponse> response = client.toBlocking().exchange(request, TraceInitResponse)

        then:
        response.status == HttpStatus.OK
        response.body().message == 'OK'
        response.body().workflowId == 'vN8KBbqR'

    }

    void 'should handle an alive request' () {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()

        and: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/alive', new TraceAliveRequest(workflowId: workflow.id))
        request = appendBasicAuth(user, request)

        HttpResponse<TraceAliveResponse> response = client.toBlocking().exchange( request, TraceAliveResponse )
        
        then:
        response.status == HttpStatus.OK
    }

    void 'should update workflow status' () {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()

        and: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow(status: WorkflowStatus.UNKNOWN)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/alive', new TraceAliveRequest(workflowId: workflow.id))
        request = appendBasicAuth(user, request)

        HttpResponse<TraceAliveResponse> response = client.toBlocking().exchange( request, TraceAliveResponse )

        then:
        response.status == HttpStatus.OK
        and:
        Workflow.withTransaction { Workflow.get(workflow.id) }.status == WorkflowStatus.RUNNING
    }

    void "save a new workflow given a start trace"() {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()
        and: 'a workflow started JSON trace'
        TraceWorkflowRequest trace = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)
        and:
        assert trace.workflow.status == WorkflowStatus.RUNNING

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/workflow', trace)
        request = appendBasicAuth(user, request)

        HttpResponse<TraceWorkflowResponse> response = client.toBlocking().exchange(
                request,
                TraceWorkflowResponse.class
        )

        then: 'the workflow has been saved successfully'
        response.status == HttpStatus.OK
        response.body().status == TraceProcessingStatus.OK
        response.body().workflowId == trace.workflow.id
        response.body().watchUrl == 'http://localhost:8000/watch/' + trace.workflow.id
        !response.body().message

        and: 'the workflow is in the database'
        Workflow.get(trace.workflow.id).status == WorkflowStatus.RUNNING
        Workflow.get(trace.workflow.id).@status == WorkflowStatus.RUNNING

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

        def response = client
                .toBlocking()
                .exchange(request, TraceTaskResponse )

        then: 'the task has been saved successfully'
        response.status == HttpStatus.OK
        response.body().status == TraceProcessingStatus.OK
        response.body().workflowId
        !response.body().message

        and: 'the task is in the database'
        with( Task.withNewTransaction { Task.list().get(0) } )  {
            taskId == 1
            status == TaskStatus.SUBMITTED
            hash == '2e/a112fb'
            process == 'sayHello'
            name == 'sayHello (2)'
            container == 'nextflow/bash'
            executor == 'aws-batch'
        }
    }

    void "try to save a new workflow without being authenticated"() {
        given: 'a workflow started JSON trace'
        TraceWorkflowRequest workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/workflow', workflowStartedJsonTrace)
        client.toBlocking().exchange( request, TraceWorkflowResponse )

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
        client.toBlocking().exchange(request, TraceTaskResponse)

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
        Workflow.withNewTransaction { Workflow.count() } == 1
        Workflow.withNewTransaction { Task.count() } == 15
    }



}

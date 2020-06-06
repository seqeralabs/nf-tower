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
import io.micronaut.context.annotation.Property
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
import io.seqera.tower.exchange.trace.TraceBeginRequest
import io.seqera.tower.exchange.trace.TraceBeginResponse
import io.seqera.tower.exchange.trace.TraceCompleteRequest
import io.seqera.tower.exchange.trace.TraceCompleteResponse
import io.seqera.tower.exchange.trace.TraceCreateRequest
import io.seqera.tower.exchange.trace.TraceCreateResponse
import io.seqera.tower.exchange.trace.TraceHeartbeatRequest
import io.seqera.tower.exchange.trace.TraceHeartbeatResponse
import io.seqera.tower.exchange.trace.TraceProgressData
import io.seqera.tower.exchange.trace.TraceProgressRequest
import io.seqera.tower.exchange.trace.TraceProgressResponse
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.service.auth.AuthenticationByApiToken
import io.seqera.tower.service.progress.ProgressStore
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import io.seqera.tower.util.TaskTraceSnapshotStatus
import io.seqera.tower.util.TracesJsonBank
import spock.lang.Timeout

@Property(name = "trace.tasks.buffer.time", value = "100ms")
@Timeout(10)
@MicronautTest(application = Application.class)
@Transactional
class TraceControllerTest2 extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject ProgressStore progressStore

    @Inject WorkflowService workflowService

    protected HttpRequest appendBasicAuth(User user, MutableHttpRequest request) {
        request.basicAuth(AuthenticationByApiToken.ID, user.accessTokens.first().token)
    }


    void 'should response to to hello' () {
        given: 'an allowed user'
        User user = new DomainCreator().createAllowedUser()

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.POST('/trace/create', new TraceCreateRequest())
        request = appendBasicAuth(user, request)

        HttpResponse<TraceCreateResponse> response = client.toBlocking().exchange(request, TraceCreateResponse)

        then:
        response.status == HttpStatus.OK
        response.body().workflowId == 'vN8KBbqR'
        response.body().message == null

    }

    void "save a new workflow given a start trace"() {
        given: 'an allowed user'
        User user = new DomainCreator().createAllowedUser()
        and: 'a workflow started JSON trace'
        TraceBeginRequest trace = TracesJsonBank.extractTraceBeginRequest('success', null)
        def workflowId = trace.workflow.id
        and:
        assert trace.workflow.status == WorkflowStatus.RUNNING

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.PUT("/trace/${workflowId}/begin", trace)
        request = appendBasicAuth(user, request)
        and:
        HttpResponse<TraceBeginResponse> response = client.toBlocking().exchange( request, TraceBeginResponse )

        then: 'the workflow has been saved successfully'
        response.status == HttpStatus.OK
        response.body().status == TraceProcessingStatus.OK
        response.body().workflowId == trace.workflow.id
        response.body().watchUrl == 'http://localhost:8000/watch/' + trace.workflow.id
        !response.body().message

        and: 'the workflow is in the database'
        Workflow.get(trace.workflow.id).status == WorkflowStatus.RUNNING
        Workflow.get(trace.workflow.id).@status == WorkflowStatus.RUNNING

    }


    def 'should complete trace workflow' () {
        given: 'an allowed user'
        def creator = new DomainCreator()
        User user = creator.createAllowedUser()
        def workflow = creator.createWorkflow(owner: user)
        TraceCompleteRequest completion = TracesJsonBank.extractTraceCompleteRequest('success', workflow.id)

        when:
        MutableHttpRequest request = HttpRequest.PUT("/trace/${workflow.id}/complete", completion)
        request = appendBasicAuth(user, request)
        and:
        HttpResponse<TraceCompleteResponse> response = client.toBlocking().exchange( request, TraceCompleteResponse )
        then:
        response.status == HttpStatus.OK
        response.body().workflowId == workflow.id
    }


    void 'should handle an heartbeat request' () {
        given: 'an allowed user'
        User user = new DomainCreator().createAllowedUser()

        and: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()
        TraceHeartbeatRequest data = new TraceHeartbeatRequest(progress: new TraceProgressData())

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.PUT("/trace/${workflow.id}/heartbeat", data)
        request = appendBasicAuth(user, request)

        HttpResponse<TraceHeartbeatResponse> response = client.toBlocking().exchange( request, TraceHeartbeatResponse )

        then:
        response.status == HttpStatus.OK
    }


    void 'should update workflow status' () {
        given: 'an allowed user'
        User user = new DomainCreator().createAllowedUser()

        and: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow(status: WorkflowStatus.UNKNOWN)
        TraceHeartbeatRequest data = new TraceHeartbeatRequest(progress: new TraceProgressData())

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.PUT("/trace/${workflow.id}/heartbeat", data)
        request = appendBasicAuth(user, request)

        HttpResponse<TraceHeartbeatResponse> response = client.toBlocking().exchange( request, TraceHeartbeatResponse )

        then:
        response.status == HttpStatus.OK
        and:
        Workflow.withTransaction { Workflow.get(workflow.id) }.status == WorkflowStatus.RUNNING
    }


    void "save a new task given a submit trace"() {
        given: 'an allowed user'
        User user = new DomainCreator().createAllowedUser()

        and: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a task submitted JSON trace'
        TraceProgressRequest taskSubmittedJsonTrace = TracesJsonBank.extractTraceProgress('success', 1, TaskTraceSnapshotStatus.SUBMITTED)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.PUT("/trace/${workflow.id}/progress", taskSubmittedJsonTrace)
        request = appendBasicAuth(user, request)

        def response = client
                .toBlocking()
                .exchange(request, TraceProgressResponse )

        then: 'the task has been saved successfully'
        response.status == HttpStatus.OK
        response.body().status == TraceProcessingStatus.OK
        response.body().workflowId
        !response.body().message

        when:
        sleep 300
        then: 'the task is in the database'
        with( Task.withNewTransaction { Task.list().get(0) } )  {
            taskId == 1
            status == TaskStatus.SUBMITTED
            hash == '2e/a112fb'
            process == 'sayHello'
            name == 'sayHello (2)'
            container == 'nextflow/bash'
            executor == 'aws-batch'
        }

        and: 'the progress data is stored'
        with(progressStore.getTraceData(workflow.id)) {
            succeeded == 1
            cached == 2
            failed == 3
            processes.size() == 2
        }
    }

    void 'try create a new workflow without being auth' () {

        when: 'send a create request'
        MutableHttpRequest request = HttpRequest.POST('/trace/create',new TraceCreateRequest())
        client.toBlocking().exchange( request, TraceCreateResponse )

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED

    }

    void "try to save a new workflow without being authenticated"() {
        given: 'a workflow started JSON trace'
        TraceBeginRequest workflowStartedJsonTrace = TracesJsonBank.extractTraceBeginRequest('success', null)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.PUT('/trace/12345/begin', workflowStartedJsonTrace)
        client.toBlocking().exchange( request, TraceBeginResponse )

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

    void "try to save a new task without being authenticated"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a task submitted JSON trace'
        TraceProgressRequest taskSubmittedJsonTrace = TracesJsonBank.extractTraceProgress('success', 1, TaskTraceSnapshotStatus.SUBMITTED)

        when: 'send a save request'
        MutableHttpRequest request = HttpRequest.PUT("trace/${workflow.id}/task", taskSubmittedJsonTrace)
        client.toBlocking().exchange(request, TraceProgressResponse)

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

}

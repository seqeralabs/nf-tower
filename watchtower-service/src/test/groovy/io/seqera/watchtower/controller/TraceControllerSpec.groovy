package io.seqera.watchtower.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.util.DomainCreator
import spock.lang.AutoCleanup
import spock.lang.Shared
import io.seqera.watchtower.pogo.enums.TraceType
import io.seqera.watchtower.pogo.enums.WorkflowStatus
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.TracesJsonBank

import javax.inject.Inject

@MicronautTest(application = Application.class)
class TraceControllerSpec extends AbstractContainerBaseSpec {

    @Inject
    EmbeddedServer embeddedServer

    @Shared @AutoCleanup
    RxHttpClient client

    void setup() {
        client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())
    }


    void "test index"() {
        given:
        HttpResponse response = client.toBlocking().exchange("/trace")

        expect:
        response.status == HttpStatus.OK
    }

    void "save a new workflow given a start trace"() {
        given: 'a workflow started JSON trace'
        Map workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace(1, WorkflowStatus.STARTED)

        when: 'send a save request'
        HttpResponse<Map> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/save', workflowStartedJsonTrace),
                Map.class
        )

        then: 'the workflow has been saved succesfully'
        response.status == HttpStatus.CREATED
        response.body().traceType == TraceType.WORKFLOW.toString()
        response.body().entityId
    }

    void "save a new task given a submit trace"() {
        given: 'a task submitted JSON trace'
        Map taskSubmittedJsonTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, TaskStatus.SUBMITTED)

        and: 'a workflow associated with the task'
        new DomainCreator().createWorkflow(runId: taskSubmittedJsonTrace.runId, runName: taskSubmittedJsonTrace.runName)

        when: 'send a save request'
        HttpResponse<Map> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/save', taskSubmittedJsonTrace),
                Map.class
        )

        then: 'the workflow has been saved succesfully'
        response.status == HttpStatus.CREATED
        response.body().traceType == TraceType.TASK.toString()
        response.body().entityId
    }

}

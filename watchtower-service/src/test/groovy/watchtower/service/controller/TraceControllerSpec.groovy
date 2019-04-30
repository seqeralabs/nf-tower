package watchtower.service.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import spock.lang.AutoCleanup
import spock.lang.Shared
import watchtower.service.pogo.enums.TaskStatus
import watchtower.service.pogo.enums.TraceType
import watchtower.service.pogo.enums.WorkflowStatus
import watchtower.service.util.AbstractContainerBaseSpec
import watchtower.service.util.DomainCreator
import watchtower.service.util.TracesJsonBank

import javax.inject.Inject

@MicronautTest(packages = 'watchtower.service.domain')
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

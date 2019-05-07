package io.seqera.watchtower.controller

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
import io.seqera.watchtower.pogo.enums.TraceType
import io.seqera.watchtower.pogo.enums.WorkflowStatus
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.DomainCreator
import io.seqera.watchtower.util.TracesJsonBank

import javax.inject.Inject

@MicronautTest(application = Application.class)
class TraceControllerSpec extends AbstractContainerBaseSpec {

    @Inject
    @Client('/')
    RxHttpClient client


    void "test index"() {
        given:
        HttpResponse response = client.toBlocking().exchange("/trace")

        expect:
        response.status == HttpStatus.OK
    }

    void "save a new workflow given a start trace"() {
        given: 'a workflow started JSON trace'
        Map workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: 'send a save request'
        HttpResponse<Map> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/save', workflowStartedJsonTrace),
                Map.class
        )

        then: 'the workflow has been saved succesfully'
        response.status == HttpStatus.CREATED
        response.body().traceType == TraceType.WORKFLOW.name()
        response.body().entityId

        and: 'the workflow is in the database'
        Workflow.count() == 1
    }

    void "save a new task given a submit trace"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a task submitted JSON trace'
        Map taskSubmittedJsonTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        when: 'send a save request'
        HttpResponse<Map> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/save', taskSubmittedJsonTrace),
                Map.class
        )

        then: 'the task has been saved succesfully'
        response.status == HttpStatus.CREATED
        response.body().traceType == TraceType.TASK.name()
        response.body().entityId

        and: 'the task is in the database'
        Task.count() == 1
    }

}

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
import io.seqera.watchtower.pogo.enums.TraceType
import io.seqera.watchtower.pogo.enums.WorkflowStatus
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.DomainCreator
import io.seqera.watchtower.util.TracesJsonBank
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Ignore

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
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
        TraceWorkflowRequest workflowStartedJsonTrace = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: 'send a save request'
        HttpResponse<TraceWorkflowResponse> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/save', workflowStartedJsonTrace),
                TraceWorkflowResponse.class
        )

        then: 'the workflow has been saved successfully'
        response.status == HttpStatus.CREATED
        response.body().traceType == TraceType.WORKFLOW
        response.body().workflowId

        and: 'the workflow is in the database'
        Workflow.count() == 1
    }

    void "save a new task given a submit trace"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a task submitted JSON trace'
        TraceWorkflowRequest taskSubmittedJsonTrace = TracesJsonBank.extractTaskJsonTrace(1, 1, workflow.id, TaskStatus.SUBMITTED)

        when: 'send a save request'
        HttpResponse<TraceWorkflowResponse> response = client.toBlocking().exchange(
                HttpRequest.POST('/trace/save', taskSubmittedJsonTrace),
                TraceWorkflowResponse.class
        )

        then: 'the task has been saved successfully'
        response.status == HttpStatus.CREATED
        response.body().traceType == TraceType.TASK
        response.body().workflowId

        and: 'the task is in the database'
        Task.count() == 1
    }

    @Ignore
    void "save traces simulated from a complete sequence"() {
        given: 'a JSON trace sequence'
        List<File> jsonFileSequence = TracesJsonBank.simulateNextflowWithTowerJsonSequence(2)

        when: 'send a save request for each trace'
        HttpResponse<Map> lastResponse
        ObjectMapper mapper = new ObjectMapper()
        jsonFileSequence.eachWithIndex { File jsonFile, int index ->
            Map jsonMap = mapper.readValue(jsonFile, Map.class)
            if (index > 0) {
                if (jsonMap.task) {
                    jsonMap.task.workflowId = lastResponse.body().workflowId
                } else if (jsonMap.workflow)
                    jsonMap.workflow.workflowId = lastResponse.body().workflowId
            }

            lastResponse = client.toBlocking().exchange(HttpRequest.POST('/trace/save', jsonMap), Map.class)
        }

        and: 'get the saved workflow'
        Workflow workflow = Workflow.get(lastResponse.body().workflowId)

        then: 'the workflow and its tasks have been saved'
        workflow
        workflow.tasks.size() == 2
        workflow.status == WorkflowStatus.SUCCEEDED
    }

}

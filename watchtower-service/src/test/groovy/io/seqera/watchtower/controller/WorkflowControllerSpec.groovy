package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.*
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowList
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class WorkflowControllerSpec extends AbstractContainerBaseSpec {

    @Inject
    @Client('/')
    RxHttpClient client


    void "get a workflow"() {
        given: "a workflow with some summaries"
        DomainCreator domainCreator = new DomainCreator()
        Workflow workflow = domainCreator.createWorkflow(
            manifest: new Manifest(defaultBranch: 'master'),
            stats: new Stats(computeTimeFmt: '(a few seconds)'),
            nextflow: new NextflowMeta(nextflowVersion: "19.05.0-TOWER"),
            summaryEntries: [domainCreator.createSummaryEntry(), domainCreator.createSummaryEntry()],
            progress: new Progress(running: 0, submitted: 0, failed: 0, pending: 0, succeeded: 0, cached: 0)
        )

        and: "perform the request to obtain the workflow"
        HttpResponse<WorkflowGet> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}"),
                WorkflowGet.class
        )

        expect: "the workflow data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflow.workflowId == workflow.id.toString()
        response.body().workflow.stats
        response.body().workflow.nextflow
        response.body().workflow.manifest
        response.body().summary.size() == 2
        response.body().progress
    }

    void "try to get a non-existing workflow"() {
        when: "perform the request to obtain a non-existing workflow"
        client.toBlocking().exchange(
                HttpRequest.GET("/workflow/100"),
                TraceWorkflowRequest.class
        )

        then: "a 404 response is obtained"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
    }

    void "get a list of workflows"() {
        given: "a workflow"
        DomainCreator domainCreator = new DomainCreator()
        Workflow workflow = domainCreator.createWorkflow(
                summaryEntries: [domainCreator.createSummaryEntry(), domainCreator.createSummaryEntry()],
                progress: new Progress(running: 0, submitted: 0, failed: 0, pending: 0, succeeded: 0, cached: 0)
        )

        and: "perform the request to obtain the workflows"
        HttpResponse<WorkflowList> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/list"),
                WorkflowList.class
        )

        expect: "the workflows data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflows.size() == 1
        response.body().workflows.first().workflow.workflowId == workflow.id.toString()
        response.body().workflows.first().progress
        response.body().workflows.first().summary.size() == 2
    }

}

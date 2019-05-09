package io.seqera.watchtower.controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
class WorkflowControllerSpec extends AbstractContainerBaseSpec {

    @Inject
    @Client('/')
    RxHttpClient client


    void "get a workflow"() {
        given: "a workflow with some summaries"
        Workflow workflow = new DomainCreator().createWorkflow(
            manifestDefaultBranch: 'master',
            computeTimeFmt: 0,
            nextflowVersion: '19.05.0-TOWER',
            magnitudeSummaries: [new DomainCreator(save: false).createMagnitudeSummary(), new DomainCreator(save: false).createMagnitudeSummary()]
        )

        and: "perform the request to obtain the workflow"
        HttpResponse<Map> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}"),
                Map.class
        )

        expect: "the workflow data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflow
        response.body().workflow.stats
        response.body().workflow.nextflow
        response.body().workflow.manifest
        response.body().progress
    }

    void "try to get a non-existing workflow"() {
        when: "perform the request to obtain a non-existing workflow"
        client.toBlocking().exchange(
                HttpRequest.GET("/workflow/100"),
                Map.class
        )

        then: "a 404 response is obtained"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
    }

}

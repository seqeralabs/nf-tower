package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.*
import io.seqera.watchtower.domain.auth.User
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowList
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator
import spock.lang.Ignore

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
@Ignore("throws 'IllegalStateException: state should be: open' when executing all tests")
class WorkflowControllerTest extends AbstractContainerBaseTest {

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
        String accessToken = doLogin(createAllowedUser())
        HttpResponse<WorkflowGet> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}")
                           .bearerAuth(accessToken),
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

    void "get a list of workflows"() {
        given: "a workflow"
        DomainCreator domainCreator = new DomainCreator()
        Workflow workflow = domainCreator.createWorkflow(
                summaryEntries: [domainCreator.createSummaryEntry(), domainCreator.createSummaryEntry()],
                progress: new Progress(running: 0, submitted: 0, failed: 0, pending: 0, succeeded: 0, cached: 0)
        )

        and: "perform the request to obtain the workflows"
        String accessToken = doLogin(createAllowedUser())
        HttpResponse<WorkflowList> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/list")
                           .bearerAuth(accessToken),
                WorkflowList.class
        )

        expect: "the workflows data is properly obtained"
        response.status == HttpStatus.OK
        response.body().workflows.size() == 1
        response.body().workflows.first().workflow.workflowId == workflow.id.toString()
        response.body().workflows.first().progress
        response.body().workflows.first().summary.size() == 2
    }

    void "try to get a non-existing workflow"() {
        when: "perform the request to obtain a non-existing workflow"
        String accessToken = doLogin(createAllowedUser())
        client.toBlocking().exchange(
                HttpRequest.GET("/workflow/100")
                        .bearerAuth(accessToken),
                TraceWorkflowRequest.class
        )

        then: "a 404 response is obtained"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.NOT_FOUND
    }

    void "try to get a workflow as a not allowed user"() {
        given: "a workflow with some summaries"
        DomainCreator domainCreator = new DomainCreator()
        Workflow workflow = domainCreator.createWorkflow()

        when: "perform the request to obtain the workflow as a not allowed user"
        String accessToken = doLogin(createNotAllowedUser())
        HttpResponse<WorkflowGet> response = client.toBlocking().exchange(
                HttpRequest.GET("/workflow/${workflow.id}")
                           .bearerAuth(accessToken),
                WorkflowGet.class
        )

        then: "a 403 response is obtained"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.FORBIDDEN
    }

    private User createAllowedUser() {
        DomainCreator domainCreator = new DomainCreator()
        User user = domainCreator.createUser()
        domainCreator.createUserRole(user: user, role: domainCreator.createRole(authority: 'ROLE_USER'))

        user
    }

    private User createNotAllowedUser() {
        DomainCreator domainCreator = new DomainCreator()
        User user = domainCreator.createUser()
        domainCreator.createUserRole(user: user, role: domainCreator.createRole(authority: 'ROLE_INVALID'))

        user
    }

    private String doLogin(User user) {
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                         .accept(MediaType.APPLICATION_JSON_TYPE)
                                         .body(new UsernamePasswordCredentials(user.email, user.authToken))
        HttpResponse<AccessRefreshToken> response = client.toBlocking().exchange(request, AccessRefreshToken)

        response.body.get().accessToken
    }

}

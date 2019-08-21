package io.seqera.tower.controller

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowTag
import io.seqera.tower.exchange.workflowTag.CreateWorkflowTagRequest
import io.seqera.tower.exchange.workflowTag.CreateWorkflowTagResponse
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class WorkflowTagControllerTest extends AbstractContainerBaseTest {

    @Inject
    @Client('/')
    RxHttpClient client

    void "create a new workflow tag"() {
        given: 'a user'
        DomainCreator creator = new DomainCreator()
        User user = creator.generateAllowedUser()

        and: 'a workflow associated with the tag'
        Workflow workflow = creator.createWorkflow(owner: user)

        and: 'the tag to create'
        WorkflowTag workflowTag = new WorkflowTag(label: 'label')

        and: 'the request object'
        CreateWorkflowTagRequest request = new CreateWorkflowTagRequest(workflowId: workflow.id, workflowTag: workflowTag)

        when: "perform the request to create the tag"
        String accessToken = doJwtLogin(user, client)
        HttpResponse<CreateWorkflowTagResponse> response = client.toBlocking().exchange(
                HttpRequest.POST('/tag/create', request)
                           .bearerAuth(accessToken),
                CreateWorkflowTagResponse.class
        )

        then: 'the tag has been created'
        response.status == HttpStatus.CREATED
        response.body().workflowTag.id
        response.body().workflowTag.label == workflowTag.label
    }

    void "try to create a new workflow tag without providing workflow id"() {
        given: 'a user'
        DomainCreator creator = new DomainCreator()
        User user = creator.generateAllowedUser()

        and: 'the tag to create'
        WorkflowTag workflowTag = new WorkflowTag(label: 'label')

        and: 'the request object'
        CreateWorkflowTagRequest request = new CreateWorkflowTagRequest(workflowId: null, workflowTag: workflowTag)

        when: "perform the request to create the tag"
        String accessToken = doJwtLogin(user, client)
        client.toBlocking().exchange(
                HttpRequest.POST('/tag/create', request)
                        .bearerAuth(accessToken),
                CreateWorkflowTagResponse.class
        )

        then: 'the tag has been created'
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.message == 'Trying to associate to nonexistent workflow'
    }

    void "try to create a new workflow tag for a workflow associated to other user"() {
        given: 'the tag to create'
        DomainCreator creator = new DomainCreator()
        WorkflowTag workflowTag = new WorkflowTag(label: 'label')

        and: 'a workflow associated with the tag'
        Workflow workflow = creator.createWorkflow()

        and: 'the request object'
        CreateWorkflowTagRequest request = new CreateWorkflowTagRequest(workflowId: workflow.id, workflowTag: workflowTag)

        when: "perform the request to create the tag as another user"
        User otherUser = creator.generateAllowedUser()
        String accessToken = doJwtLogin(otherUser, client)
        client.toBlocking().exchange(
                HttpRequest.POST('/tag/create', request)
                           .bearerAuth(accessToken),
                CreateWorkflowTagResponse.class
        )

        then: 'the tag has been created'
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.message == 'Trying to associate to a not owned workflow'
    }

}

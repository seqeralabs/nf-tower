package io.seqera.tower.controller

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpParameters
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowTag
import io.seqera.tower.exchange.workflow.ListWorklowResponse
import io.seqera.tower.exchange.workflow.WorkflowGet
import io.seqera.tower.exchange.workflowTag.CreateWorkflowTagRequest
import io.seqera.tower.exchange.workflowTag.CreateWorkflowTagResponse
import io.seqera.tower.service.UserService
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.service.WorkflowTagService
import org.grails.datastore.mapping.validation.ValidationException

import javax.inject.Inject

@Controller("/tag")
@Slf4j
class WorkflowTagController {

    WorkflowTagService workflowTagService
    WorkflowService workflowService
    UserService userService

    @Inject
    WorkflowController(WorkflowTagService workflowTagService, WorkflowService workflowService, UserService userService) {
        this.workflowTagService = workflowTagService
        this.workflowService = workflowService
        this.userService = userService
    }


    @Post("/create")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<CreateWorkflowTagResponse> create(@Body CreateWorkflowTagRequest request, Authentication authentication) {
        try {
            Workflow workflow = workflowService.get(request.workflowId as Serializable)
            if (!workflow) {
                return HttpResponse.badRequest(CreateWorkflowTagResponse.ofError('Trying to associate to nonexistent workflow'))
            }

            User currentUser = userService.getFromAuthData(authentication)
            if (workflow.ownerId != currentUser.id) {
                return HttpResponse.badRequest(CreateWorkflowTagResponse.ofError('Trying to associate to a not owned workflow'))
            }

            WorkflowTag workflowTag = workflowTagService.create(request.workflowTag, workflow)
            return HttpResponse.created(CreateWorkflowTagResponse.ofTag(workflowTag))
        } catch (ValidationException e) {
            return HttpResponse.badRequest(CreateWorkflowTagResponse.ofError('Validation error creating workflow tag'))
        } catch (Exception e) {
            log.error("Unexpected error creating workflow tag -- request=$request", e)
            return HttpResponse.badRequest(CreateWorkflowTagResponse.ofError('Unexpected error creating workflow tag'))
        }

    }
}

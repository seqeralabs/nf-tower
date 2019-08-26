package io.seqera.tower.controller

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowTag
import io.seqera.tower.exchange.workflowTag.CreateWorkflowTagRequest
import io.seqera.tower.exchange.workflowTag.CreateWorkflowTagResponse
import io.seqera.tower.exchange.workflowTag.UpdateWorkflowTagRequest
import io.seqera.tower.exchange.workflowTag.UpdateWorkflowTagResponse
import io.seqera.tower.service.UserService
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.service.WorkflowTagService
import org.grails.datastore.mapping.validation.ValidationException
import org.springframework.context.MessageSource

import javax.inject.Inject

@Controller("/tag")
@Slf4j
class WorkflowTagController {

    WorkflowTagService workflowTagService
    WorkflowService workflowService
    UserService userService

    MessageSource messageSource

    @Inject
    WorkflowController(WorkflowTagService workflowTagService, WorkflowService workflowService, UserService userService, MessageSource messageSource) {
        this.workflowTagService = workflowTagService
        this.workflowService = workflowService
        this.userService = userService

        this.messageSource = messageSource
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
            String firstErrorMessage = messageSource.getMessage(e.getErrors().fieldError, Locale.ENGLISH)
            return HttpResponse.badRequest(CreateWorkflowTagResponse.ofError(firstErrorMessage))
        } catch (Exception e) {
            log.error("Unexpected error creating workflow tag -- request=$request", e)
            return HttpResponse.badRequest(CreateWorkflowTagResponse.ofError('Unexpected error creating workflow tag'))
        }
    }

    @Put("/update")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<UpdateWorkflowTagResponse> update(@Body UpdateWorkflowTagRequest request, Authentication authentication) {
        try {
            WorkflowTag existingWorkflowTag = workflowTagService.get(request.updateWorkflowTag.id)
            if (!existingWorkflowTag) {
                return HttpResponse.badRequest(UpdateWorkflowTagResponse.ofError('Trying to update nonexistent workflow tag'))
            }

            User currentUser = userService.getFromAuthData(authentication)
            if (existingWorkflowTag.workflow.ownerId != currentUser.id) {
                return HttpResponse.badRequest(UpdateWorkflowTagResponse.ofError('Trying to update a not owned tag'))
            }

            WorkflowTag workflowTag = workflowTagService.update(existingWorkflowTag, request.updateWorkflowTag)
            return HttpResponse.ok(UpdateWorkflowTagResponse.ofTag(workflowTag))
        } catch (ValidationException e) {
            String firstErrorMessage = messageSource.getMessage(e.getErrors().fieldError, Locale.ENGLISH)
            return HttpResponse.badRequest(UpdateWorkflowTagResponse.ofError(firstErrorMessage))
        } catch (Exception e) {
            log.error("Unexpected error creating workflow tag -- request=$request", e)
            return HttpResponse.badRequest(UpdateWorkflowTagResponse.ofError('Unexpected error creating workflow tag'))
        }
    }


}

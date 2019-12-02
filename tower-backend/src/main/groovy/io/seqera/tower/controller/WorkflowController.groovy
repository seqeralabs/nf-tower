/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.controller

import javax.inject.Inject

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpParameters
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.exchange.MessageResponse
import io.seqera.tower.exchange.progress.GetProgressResponse
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.task.TaskGet
import io.seqera.tower.exchange.task.TaskList
import io.seqera.tower.exchange.workflow.AddWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.AddWorkflowCommentResponse
import io.seqera.tower.exchange.workflow.DeleteWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.DeleteWorkflowCommentResponse
import io.seqera.tower.exchange.workflow.GetWorkflowMetricsResponse
import io.seqera.tower.exchange.workflow.GetWorkflowResponse
import io.seqera.tower.exchange.workflow.ListWorkflowCommentsResponse
import io.seqera.tower.exchange.workflow.ListWorklowResponse
import io.seqera.tower.exchange.workflow.UpdateWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.UpdateWorkflowCommentResponse
import io.seqera.tower.service.TaskService
import io.seqera.tower.service.UserService
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.service.audit.AuditEventPublisher
import io.seqera.tower.service.progress.ProgressService
import io.seqera.tower.validation.ValidationHelper
import org.grails.datastore.mapping.validation.ValidationException
/**
 * Implements the `workflow` API
 */
@Controller("/workflow")
@Slf4j
class WorkflowController extends BaseController {

    @Inject WorkflowService workflowService
    @Inject TaskService taskService
    @Inject UserService userService
    @Inject ProgressService progressService
    @Inject AuditEventPublisher eventPublisher

    protected ProgressData getProgressData(Workflow workflow) {
        def result = progressService.getProgressData(workflow.id)
        if( result == null ) {
            log.warn "Cannot find workflow stats for Id=$workflow.id -- Fallback on legacy progress query"
            result = progressService.getProgressQuery(workflow)
        }
        if( result == null ) {
            log.error "Cannot retrive progress stats for workflow with Id=$workflow.id"
            result = ProgressData.EMPTY
        }
        return result
    }

    @Get("/list")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<ListWorklowResponse> list(Authentication authentication, HttpParameters filterParams) {
        Long max = filterParams.getFirst('max', Long.class, 50l)
        Long offset = filterParams.getFirst('offset', Long.class, 0l)

        String search = filterParams.getFirst('search', String.class, '')
        String searchRegex = search ? search.contains('*') ? search.replaceAll(/\*/, '%') : "${search}%" : null

        List<Workflow> workflows = workflowService.listByOwner(userService.getByAuth(authentication), max, offset, searchRegex)

        List<GetWorkflowResponse> result = workflows.collect { Workflow workflow ->
            GetWorkflowResponse.of(workflow)
        }
        HttpResponse.ok(ListWorklowResponse.of(result))
    }

    /**
     * Endpoint invoked by the client to fetch workflow status
     *
     * @param workflowId The ID of the workflow for which the status is requested
     * @return The http response
     */
    @Get("/{workflowId}")
    @Transactional(readOnly = true)
    @Secured(['ROLE_USER'])
    HttpResponse<GetWorkflowResponse> get(String workflowId, Authentication authentication) {
        final workflow = workflowService.get(workflowId)
        if (!workflow)
            return HttpResponse.notFound(GetWorkflowResponse.error("Unknown workflow ID: $workflowId"))
        final user = userService.getByAuth(authentication)
        if( !user ) {
            log.error "Unknown user=${authentication.name}"
            return HttpResponse.badRequest(GetWorkflowResponse.error("Invalid user authenticaton: $authentication.name"))
        }
        if( workflow.owner.id != user.id ) {
            log.warn "Workflow ID=$workflowId does not belong to user=$authentication.name"
            return HttpResponse.badRequest(GetWorkflowResponse.error("Invalid workflow request: $workflowId"))
        }

        final resp = new GetWorkflowResponse(workflow: workflow)
        // fetch progress
        resp.progress = getProgressData(workflow)

        HttpResponse.ok(resp)
    }

    /**
     * Endpoint invoked by the client to fetch workflow progress data
     *
     * @param workflowId The ID of the workflow for which the update is requested
     * @return The http response
     */
    @Transactional(readOnly = true)
    @Secured(['ROLE_USER'])
    @Get('/{workflowId}/progress')
    @CompileDynamic
    HttpResponse<GetProgressResponse> progress(String workflowId) {
        try {
            final Workflow workflow = workflowService.get(workflowId)
            if (!workflow) {
                return HttpResponse.notFound(new GetProgressResponse(message: "Oops... Can't find workflow ID $workflowId"))
            }

            final ProgressData progress = getProgressData(workflow)
            HttpResponse.ok(new GetProgressResponse(progress: progress))
        }
        catch( Exception e ) {
            log.error "Unable to get progress for workflow with id=$workflowId", e
            HttpResponse.badRequest(new GetProgressResponse(message: "Oops... Failed to get progress for workflow ID $workflowId"))
        }
    }

    @Get("/{workflowId}/tasks")
    @Transactional
    @Secured(SecurityRule.IS_ANONYMOUS)
    HttpResponse<TaskList> tasks(String workflowId, HttpParameters filterParams) {
        int max = filterParams.getFirst('length', Integer.class, 10)
        Long offset = filterParams.getFirst('start', Long.class, 0l)
        String orderProperty = filterParams.getFirst('order[0][column]', String.class, 'taskId')
        String orderDir = filterParams.getFirst('order[0][dir]', String.class, 'asc')
        String search = filterParams.getFirst('search', String.class, null)

        List<Task> tasks = taskService.findTasks(workflowId, search, orderProperty, orderDir, max, offset)
        List<TaskGet> result = tasks.collect {
            TaskGet.of(it)
        }

        long total = result.size()==max ? offset+max+1 : offset+result.size()
        HttpResponse.ok(TaskList.of(result, total))
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Delete('/{workflowId}')
    HttpResponse delete(String workflowId, Authentication authentication) {
        final user = userService.getByAuth(authentication)
        eventPublisher.workflowDeletion(workflowId)
        if( workflowService.markForDeletion(workflowId) )
            HttpResponse.status(HttpStatus.NO_CONTENT)
        else
            HttpResponse.badRequest(new MessageResponse("Oops... Failed to delete workflow with ID $workflowId"))
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Get('/{workflowId}/metrics')
    @CompileDynamic
    HttpResponse<GetWorkflowMetricsResponse> metrics(String workflowId) {
        try {
            final workflow = workflowService.get(workflowId)
            if (!workflow)
                return HttpResponse.notFound(new GetWorkflowMetricsResponse(message:"Oops... Can't find workflow ID $workflowId"))

            final result = workflowService.findMetrics(workflow)
            HttpResponse.ok(new GetWorkflowMetricsResponse(metrics: new ArrayList<WorkflowMetrics>(result)))
        }
        catch( Exception e ) {
            log.error "Unable to get metrics for workflow with id=$workflowId", e
            HttpResponse.badRequest(new GetWorkflowMetricsResponse(message:"Oops... Failed to get execution metrics for workflow ID $workflowId"))
        }
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Get('/{workflowId}/comments')
    @CompileDynamic
    HttpResponse<ListWorkflowCommentsResponse> listComments(String workflowId) {
        try {
            final workflow = workflowService.get(workflowId)
            if (!workflow)
                return HttpResponse.notFound(new ListWorkflowCommentsResponse(message:"Oops... Can't find workflow ID $workflowId"))

            final result = workflowService.getComments(workflow)
            HttpResponse.ok(ListWorkflowCommentsResponse.of(result))
        }
        catch( Exception e ) {
            log.error "Unable to get comments for workflow with id=$workflowId", e
            HttpResponse.badRequest(new ListWorkflowCommentsResponse(message:"Oops... Failed to get comments for workflow ID $workflowId"))
        }
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Post('/{workflowId}/comment/add')
    @CompileDynamic
    HttpResponse<AddWorkflowCommentResponse> addComment(Authentication authentication, String workflowId, AddWorkflowCommentRequest request) {
        try {
            final user = userService.getByAuth(authentication)
            final workflow = workflowService.get(workflowId)
            if (!workflow)
                return HttpResponse.notFound(new AddWorkflowCommentResponse(message:"Oops... Can't find workflow ID $workflowId"))

            final comment = new WorkflowComment()
            comment.user = user
            comment.workflow = workflow
            comment.text = request.text
            comment.dateCreated = request.timestamp
            comment.lastUpdated = request.timestamp
            final record = comment.save(failOnError: true)

            return HttpResponse.ok( AddWorkflowCommentResponse.withComment(record) )
        }
        catch(ValidationException e) {
            final msg = "Oops... Unable to add comment -- " + ValidationHelper.formatErrors(e)
            final resp = AddWorkflowCommentResponse.withMessage(msg)
            return HttpResponse.badRequest(resp)
        }
        catch (Exception e) {
            log.error("Unexpected error adding workflow comment -- request=$request", e)
            final msg = "Unexpected error adding workflow comment"
            final resp = AddWorkflowCommentResponse.withMessage(msg)
            return HttpResponse.badRequest(resp)
        }
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Put('/{workflowId}/comment')
    @CompileDynamic
    HttpResponse<UpdateWorkflowCommentResponse> updateComment(Authentication authentication, String workflowId, UpdateWorkflowCommentRequest request) {
        try {
            final user = userService.getByAuth(authentication)

            // check `commentId` and `workflowId` are provided
            if( !request.commentId )
                return HttpResponse.badRequest(new UpdateWorkflowCommentResponse(message:"Oops.. Missing comment ID"))
            if( !workflowId )
                return HttpResponse.badRequest(new UpdateWorkflowCommentResponse(message:"Oops.. Missing workflow ID"))

            // make sure the comment exists
            final comment = WorkflowComment.get(request.commentId)
            if (!comment)
                return HttpResponse.notFound(new UpdateWorkflowCommentResponse(message:"Oops... Can't find workflow comment with ID $request.commentId"))

            // user can only modify its own comment
            if( comment.user.id != user.id )
                return HttpResponse.badRequest(new UpdateWorkflowCommentResponse(message:"Oops.. You are not allowed to modify this comment"))

            // make sure it match the workflow id
            if( comment.workflow.id != workflowId )
                return HttpResponse.badRequest(new UpdateWorkflowCommentResponse(message:"Oops.. Mismatch comment workflow id"))

            comment.text = request.text
            comment.lastUpdated = request.timestamp
            comment.save(failOnError: true).id

            return HttpResponse.ok( new UpdateWorkflowCommentResponse() )
        }
        catch(ValidationException e) {
            final msg = "Oops... Unable to update comment -- " + ValidationHelper.formatErrors(e)
            final resp = new UpdateWorkflowCommentResponse(message: msg)
            return HttpResponse.badRequest(resp)
        }
        catch (Exception e) {
            log.error("Unexpected error updating workflow comment -- request=$request", e)
            final msg = "Unexpected error update workflow comment"
            final resp = new UpdateWorkflowCommentResponse(message: msg)
            return HttpResponse.badRequest(resp)
        }
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Delete('/{workflowId}/comment')
    @CompileDynamic
    HttpResponse<DeleteWorkflowCommentResponse> deleteComment(Authentication authentication, String workflowId, DeleteWorkflowCommentRequest request) {
        try {
            final user = userService.getByAuth(authentication)

            // check `commentId` and `workflowId` are provided
            if( !request.commentId )
                return HttpResponse.badRequest(new DeleteWorkflowCommentResponse(message:"Oops.. Missing comment ID"))
            if( !workflowId )
                return HttpResponse.badRequest(new DeleteWorkflowCommentResponse(message:"Oops.. Missing workflow ID"))

            // make sure the comment exists
            final comment = WorkflowComment.get(request.commentId)
            if (!comment)
                return HttpResponse.notFound(new DeleteWorkflowCommentResponse(message:"Oops... Can't find workflow comment with ID $request.commentId"))

            // user can only modify it's own comment
            if( comment.user.id != user.id )
                return HttpResponse.badRequest(new DeleteWorkflowCommentResponse(message:"Oops.. You are not allowed to delete this comment"))

            // make sure it match the workflow id
            if( comment.workflow.id != workflowId )
                return HttpResponse.badRequest(new DeleteWorkflowCommentResponse(message:"Oops.. Mismatch comment workflow id"))

            comment.delete(failOnError: true)
            return HttpResponse.ok( new DeleteWorkflowCommentResponse() )
        }
        catch(ValidationException e) {
            final msg = "Oops... Unable to delete comment -- " + ValidationHelper.formatErrors(e)
            final resp = new DeleteWorkflowCommentResponse(message: msg)
            return HttpResponse.badRequest(resp)
        }
        catch (Exception e) {
            log.error("Unexpected error deleting workflow comment -- request=$request", e)
            final msg = "Unexpected error deleting workflow comment"
            final resp = new DeleteWorkflowCommentResponse(message: msg)
            return HttpResponse.badRequest(resp)
        }
    }
}

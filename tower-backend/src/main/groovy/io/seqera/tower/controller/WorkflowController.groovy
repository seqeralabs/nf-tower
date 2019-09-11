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

import io.seqera.tower.exchange.progress.GetProgressResponse
import io.seqera.tower.exchange.progress.ProgressData

import javax.inject.Inject

import grails.gorm.PagedResultList
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
import io.seqera.tower.exchange.task.TaskGet
import io.seqera.tower.exchange.task.TaskList
import io.seqera.tower.exchange.workflow.AddWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.AddWorkflowCommentResponse
import io.seqera.tower.exchange.workflow.DeleteWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.DeleteWorkflowCommentResponse
import io.seqera.tower.exchange.workflow.ListWorkflowCommentsResponse
import io.seqera.tower.exchange.workflow.GetWorkflowMetricsResponse
import io.seqera.tower.exchange.workflow.UpdateWorkflowCommentRequest
import io.seqera.tower.exchange.workflow.UpdateWorkflowCommentResponse
import io.seqera.tower.exchange.workflow.WorkflowGet
import io.seqera.tower.exchange.workflow.ListWorklowResponse
import io.seqera.tower.service.ProgressService
import io.seqera.tower.service.TaskService
import io.seqera.tower.service.UserService
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.validation.ValidationHelper
import org.grails.datastore.mapping.validation.ValidationException
/**
 * Implements the `workflow` API
 */
@Controller("/workflow")
@Slf4j
class WorkflowController extends BaseController {

    WorkflowService workflowService
    TaskService taskService
    ProgressService progressService
    UserService userService

    @Inject
    WorkflowController(WorkflowService workflowService, TaskService taskService, ProgressService progressService, UserService userService) {
        this.workflowService = workflowService
        this.taskService = taskService
        this.progressService = progressService
        this.userService = userService
    }


    @Get("/list")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<ListWorklowResponse> list(Authentication authentication, HttpParameters filterParams) {
        Long max = filterParams.getFirst('max', Long.class, 50l)
        Long offset = filterParams.getFirst('offset', Long.class, 0l)

        String search = filterParams.getFirst('search', String.class, '')
        String searchRegex = search ? search.contains('*') ? search.replaceAll(/\*/, '%') : "${search}%" : null

        List<Workflow> workflows = workflowService.listByOwner(userService.getFromAuthData(authentication), max, offset, searchRegex)

        List<WorkflowGet> result = workflows.collect { Workflow workflow ->
            WorkflowGet.of(workflow)
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
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<WorkflowGet> get(String workflowId) {
        Workflow workflow = workflowService.get(workflowId)

        if (!workflow) {
            return HttpResponse.notFound()
        }
        HttpResponse.ok(progressService.buildWorkflowGet(workflow))
    }

    /**
     * Endpoint invoked by the client to fetch workflow progress data
     *
     * @param workflowId The ID of the workflow for which the update is requested
     * @return The http response
     */
    @Transactional
    @Secured(['ROLE_USER'])
    @Get('/{workflowId}/progress')
    @CompileDynamic
    HttpResponse<GetProgressResponse> progress(String workflowId) {
        try {
            final Workflow workflow = workflowService.get(workflowId)
            if (!workflow) {
                return HttpResponse.notFound(new GetProgressResponse(message: "Oops... Can't find workflow ID $workflowId"))
            }
            // TODO check the user is allowed to fetch this data

            final ProgressData progress = progressService.fetchWorkflowProgress(workflow)
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
        Long max = filterParams.getFirst('length', Long.class, 10l)
        Long offset = filterParams.getFirst('start', Long.class, 0l)
        String orderProperty = filterParams.getFirst('order[0][column]', String.class, 'taskId')
        String orderDir = filterParams.getFirst('order[0][dir]', String.class, 'asc')

        String search = filterParams.getFirst('search', String.class, '')
        String searchRegex = search ? search.contains('*') ? search.replaceAll(/\*/, '%') : "${search}%" : null

        PagedResultList<Task> taskPagedResultList = taskService.findTasks(workflowId, max, offset, orderProperty, orderDir, searchRegex)

        List<TaskGet> result = taskPagedResultList.collect {
            TaskGet.of(it)
        }
        HttpResponse.ok(TaskList.of(result, taskPagedResultList.totalCount))
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Delete('/{workflowId}')
    HttpResponse delete(String workflowId) {
        try {
            workflowService.deleteById(workflowId)
            HttpResponse.status(HttpStatus.NO_CONTENT)
        }
        catch( Exception e ) {
            log.error "Unable to delete workflow with id=$workflowId", e
            HttpResponse.badRequest(new MessageResponse("Oops... Failed to delete workflow with ID $workflowId"))
        }
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
            final user = userService.getFromAuthData(authentication)
            final workflow = workflowService.get(workflowId)
            if (!workflow)
                return HttpResponse.notFound(new AddWorkflowCommentResponse(message:"Oops... Can't find workflow ID $workflowId"))

            final comment = new WorkflowComment()
            comment.author = user
            comment.workflow = workflow
            comment.text = request.text
            comment.dateCreated = request.timestamp
            comment.lastUpdated = request.timestamp
            final commentId = comment.save(failOnError: true).id

            return HttpResponse.ok( AddWorkflowCommentResponse.withId(commentId) )
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
            final user = userService.getFromAuthData(authentication)

            // check `commentId` and `workflowId` are provided
            if( !request.commentId )
                return HttpResponse.badRequest(new UpdateWorkflowCommentResponse(message:"Oops.. Missing comment ID"))
            if( !workflowId )
                return HttpResponse.badRequest(new UpdateWorkflowCommentResponse(message:"Oops.. Missing workflow ID"))

            // make sure the comment exists
            final comment = WorkflowComment.get(request.commentId)
            if (!comment)
                return HttpResponse.notFound(new UpdateWorkflowCommentResponse(message:"Oops... Can't find workflow comment with ID $request.commentId"))

            // user can only modify it's own comment
            if( comment.author.id != user.id )
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
            final user = userService.getFromAuthData(authentication)

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
            if( comment.author.id != user.id )
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

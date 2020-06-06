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
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.seqera.tower.domain.Task
import io.seqera.tower.enums.TraceProcessingStatus
import io.seqera.tower.exchange.trace.TraceAliveRequest
import io.seqera.tower.exchange.trace.TraceAliveResponse
import io.seqera.tower.exchange.trace.TraceBeginRequest
import io.seqera.tower.exchange.trace.TraceBeginResponse
import io.seqera.tower.exchange.trace.TraceCompleteRequest
import io.seqera.tower.exchange.trace.TraceCompleteResponse
import io.seqera.tower.exchange.trace.TraceCreateRequest
import io.seqera.tower.exchange.trace.TraceCreateResponse
import io.seqera.tower.exchange.trace.TraceHeartbeatRequest
import io.seqera.tower.exchange.trace.TraceHeartbeatResponse
import io.seqera.tower.exchange.trace.TraceInitRequest
import io.seqera.tower.exchange.trace.TraceInitResponse
import io.seqera.tower.exchange.trace.TraceProgressRequest
import io.seqera.tower.exchange.trace.TraceProgressResponse
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceTaskResponse
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.exchange.trace.TraceWorkflowResponse
import io.seqera.tower.service.TraceService
import io.seqera.tower.service.UserService
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.service.live.LiveEventsService
/**
 * Implements the `trace` API
 *
 */
@Slf4j
@CompileStatic
@Secured(['ROLE_USER'])
@Controller("/trace")
class TraceController extends BaseController {

    @Value('${tower.server-url}')
    String serverUrl

    @Inject TraceService traceService
    @Inject UserService userService
    @Inject LiveEventsService liveEventsService
    @Inject WorkflowService workflowService

    @Post("/alive")
    @Transactional
    @Deprecated
    HttpResponse<TraceAliveResponse> alive(@Body TraceAliveRequest req) {
        log.debug "Receiving trace alive [workflowId=${req.workflowId}]"
        traceService.keepAlive(req.workflowId)
        HttpResponse.ok(new TraceAliveResponse(message: 'OK'))
    }

    @Post("/workflow")
    @Transactional
    @Deprecated
    HttpResponse<TraceWorkflowResponse> workflow(@Body TraceWorkflowRequest req, Authentication authentication) {
        try {
            final msg = (req.workflow.checkIsRunning()
                    ? "Receiving trace for new workflow [workflowId=${req.workflow.id}; user=${authentication.name}]"
                    : "Receiving trace for workflow completion [workflowId=${req.workflow.id}; user=${authentication.name}]")
            log.info(msg)

            final user = userService.getByAuth(authentication)
            final workflow = traceService.processWorkflowTrace(req, user)

            final resp = new TraceWorkflowResponse(
                    status: TraceProcessingStatus.OK,
                    workflowId: workflow.id,
                    watchUrl: "${serverUrl}/watch/${workflow.id}"
            )
            liveEventsService.publishWorkflowEvent(workflow)
            return HttpResponse.ok(resp)
        }
        catch (Exception e) {
            log.error("Failed to handle workflow trace=${req.workflow.id}", e)
            return HttpResponse.badRequest(TraceWorkflowResponse.ofError(e.message))
        }
    }

    @Post("/task")
    @Transactional
    @Deprecated
    HttpResponse<TraceTaskResponse> task(@Body TraceTaskRequest req, Authentication authentication) {
        log.info "Receiving task trace request [workflowId=${req.workflowId}; tasks=${req.tasks?.size()}; user=${authentication.name}]"

        HttpResponse<TraceTaskResponse> response
        if( !req.workflowId )
            return HttpResponse.badRequest(TraceTaskResponse.ofError("Missing workflow ID"))

        if( req.tasks?.size()>100 ) {
            log.warn "Too many tasks for workflow Id=$req.workflowId; size=${req.tasks.size()}"
            return HttpResponse.badRequest(TraceTaskResponse.ofError("Workflow trace request too big"))
        }

        try {
            List<Task> tasks = traceService.processTaskTrace(req)
            final workflow = tasks.first().workflow
            liveEventsService.publishProgressEvent(workflow)
            response = HttpResponse.ok(TraceTaskResponse.ofSuccess(workflow.id))
        }
        catch (Exception e) {
            log.error("Failed to handle tasks trace for request: $req", e)
            response = HttpResponse.badRequest(TraceTaskResponse.ofError(e.message))
        }

        return response
    }

    @Post("/init")
    @Deprecated
    HttpResponse<TraceInitResponse> init(TraceInitRequest req, Authentication authentication) {
        log.info "Receiving trace init [user=${authentication.getName()}]"
        final workflowId = workflowService.createWorkflowKey()
        final resp = new TraceInitResponse(workflowId: workflowId)
        log.info "Created new workflow ID=${workflowId} [user=${authentication.getName()}]"
        HttpResponse.ok(resp)
    }


    // --== new api ==--

    @Post("/create")
    HttpResponse<TraceCreateResponse> flowCreate(TraceCreateRequest req, Authentication authentication) {
        log.info "> Trace create request [user=${authentication.getName()}]"
        // note: when the execution is launched from Tower the workflowId is already given
        // and the a workflow entity should exists
        if( req.workflowId && !workflowService.get(req.workflowId) )
            return HttpResponse.badRequest(new TraceCreateResponse(message: "Unknown workflow launch id=$req.workflowId"))

        final workflowId = req.workflowId ?: workflowService.createWorkflowKey()
        final resp = new TraceCreateResponse(workflowId: workflowId)
        log.info "> Created new workflow id=${workflowId} [user=${authentication.getName()}]${req.workflowId ? ' [from tower]' :''}"
        HttpResponse.ok(resp)
    }

    @Put("/{workflowId}/begin")
    @Transactional
    HttpResponse<TraceBeginResponse> flowBegin(final String workflowId, @Body TraceBeginRequest req, Authentication authentication) {
        try {
            log.info( "> Receiving trace for workflow begin [workflowId=${workflowId}; user=${authentication.name}]")

            final user = userService.getByAuth(authentication)
            final workflow = traceService.handleFlowBegin(req, user)

            final resp = new TraceBeginResponse(
                            status: TraceProcessingStatus.OK,
                            workflowId: workflow.id,
                            watchUrl: "${serverUrl}/watch/${workflow.id}" )
            liveEventsService.publishWorkflowEvent(workflow)
            return HttpResponse.ok(resp)
        }
        catch (Exception e) {
            log.error("Failed to handle workflow trace=${workflowId}", e)
            return HttpResponse.badRequest(TraceBeginResponse.ofError(e.message))
        }
    }

    @Put("/{workflowId}/complete")
    @Transactional
    HttpResponse<TraceCompleteResponse> flowComplete(final String workflowId, @Body TraceCompleteRequest req, Authentication authentication) {
        try {
            log.info("> Receiving trace for workflow completion [workflowId=${workflowId}; user=${authentication.name}]")

            final user = userService.getByAuth(authentication)
            final workflow = traceService.handleFlowComplete(req, user)

            final resp = new TraceCompleteResponse(
                                status: TraceProcessingStatus.OK,
                                workflowId: workflow.id )
            liveEventsService.publishWorkflowEvent(workflow)
            return HttpResponse.ok(resp)
        }
        catch (Exception e) {
            log.error("Failed to handle workflow trace=${workflowId}", e)
            return HttpResponse.badRequest(TraceCompleteResponse.ofError(e.message))
        }
    }


    @Put("/{workflowId}/progress")
    @Transactional
    HttpResponse<TraceProgressResponse> record(String workflowId, @Body TraceProgressRequest req, Authentication authentication) {
        log.info "> Receiving trace tasks progress [workflowId=${workflowId}; tasks=${req.tasks?.size()}; user=${authentication.name}]"

        HttpResponse<TraceProgressResponse> response
        if( req.tasks?.size()>100 ) {
            log.warn "Too many tasks for workflow Id=$workflowId; size=${req.tasks.size()}"
            return HttpResponse.badRequest(TraceProgressResponse.ofError("Workflow trace request too big"))
        }

        def workflow = workflowService.get(workflowId)
        if( !workflow )
            return HttpResponse.badRequest(TraceProgressResponse.ofError("Unknown workflow Id=$workflowId"))

        try {
            traceService.handleTaskTrace(workflowId, req.progress, req.tasks)
            liveEventsService.publishProgressEvent(workflow)
            response = HttpResponse.ok(TraceProgressResponse.ofSuccess(workflow.id))

        }
        catch (Exception e) {
            log.error("Failed to handle tasks trace for request: $req", e)
            response = HttpResponse.badRequest(TraceProgressResponse.ofError(e.message))
        }

        return response
    }

    @Put("/{workflowId}/heartbeat")
    @Transactional
    HttpResponse<TraceHeartbeatResponse> heartbeat(String workflowId, @Body TraceHeartbeatRequest req) {
        log.debug "> Receiving trace heartbeat [workflowId=${workflowId}]"
        traceService.handleHeartbeat(workflowId, req.progress)
        HttpResponse.ok(new TraceHeartbeatResponse(message: 'OK'))
    }
}

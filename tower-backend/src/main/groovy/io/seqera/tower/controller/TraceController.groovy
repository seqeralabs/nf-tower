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
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.sse.Event
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.TraceProcessingStatus
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.trace.TraceAliveRequest
import io.seqera.tower.exchange.trace.TraceAliveResponse
import io.seqera.tower.exchange.trace.TraceHelloResponse
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceTaskResponse
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.exchange.trace.TraceWorkflowResponse
import io.seqera.tower.exchange.trace.sse.TraceSseResponse
import io.seqera.tower.exchange.workflow.WorkflowGet
import io.seqera.tower.service.ProgressService
import io.seqera.tower.service.ServerSentEventsService
import io.seqera.tower.service.TraceService
import io.seqera.tower.service.UserService
/**
 * Implements the `trace` API
 *
 */
@Controller("/trace")
@Secured(SecurityRule.IS_ANONYMOUS)
@Slf4j
class TraceController extends BaseController {

    @Value('${tower.server-url}')
    String serverUrl

    TraceService traceService
    ProgressService progressService
    UserService userService
    ServerSentEventsService serverSentEventsService


    @Inject
    TraceController(TraceService traceService, ProgressService progressService, UserService userService, ServerSentEventsService serverSentEventsService) {
        this.traceService = traceService
        this.progressService = progressService
        this.userService = userService
        this.serverSentEventsService = serverSentEventsService
    }


    protected void publishHeartbeatEvents(Workflow workflow) {
        String workflowFlowableKey = serverSentEventsService.getKeyForEntity(Workflow, workflow.id)
        String userFlowableKey = serverSentEventsService.getKeyForEntity(User, workflow.ownerId)

        if (workflow.checkIsStarted()) {
            log.info("Client heartbeats received for flowables: ${workflowFlowableKey} and ${userFlowableKey}")
            serverSentEventsService.tryPublish(workflowFlowableKey) {
                Event.of(TraceSseResponse.ofHeartbeat("NF heartbeat [${workflowFlowableKey}]"))
            }
            serverSentEventsService.tryPublish(userFlowableKey) {
                Event.of(TraceSseResponse.ofHeartbeat("NF heartbeat [${userFlowableKey}]"))
            }
        }
    }

    private void publishWorkflowEvent(Workflow workflow, User user) {
        String userFlowableKey = serverSentEventsService.getKeyForEntity(User, user.id)
        serverSentEventsService.tryPublish(userFlowableKey) {
            Event.of(TraceSseResponse.ofWorkflow(WorkflowGet.of(workflow)))
        }

        if (!workflow.checkIsStarted()) {
            String workflowFlowableKey = serverSentEventsService.getKeyForEntity(Workflow, workflow.id)
            serverSentEventsService.tryPublish(workflowFlowableKey) {
                final workflowWithProgress = progressService.buildWorkflowGet(workflow)
                Event.of(TraceSseResponse.ofWorkflow(workflowWithProgress))
            }
            serverSentEventsService.tryComplete(workflowFlowableKey)
        }
    }

    private void publishProgressEvent(Workflow workflow) {
        String workflowFlowableKey = serverSentEventsService.getKeyForEntity(Workflow, workflow.id)

        serverSentEventsService.tryPublish(workflowFlowableKey) {
            ProgressData progress = progressService.fetchWorkflowProgress(workflow)
            Event.of(TraceSseResponse.ofProgress(progress))
        }
    }

    @Post("/alive")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceAliveResponse> alive(@Body TraceAliveRequest req) {
        log.info "Trace alive message for workflowId=$req.workflowId"

        Workflow workflow = Workflow.get(req.workflowId)
        if (!workflow) {
            final msg = "Cannot find workflowId=$req.workflowId"
            log.warn(msg)
            return HttpResponse.badRequest(new TraceAliveResponse(message:msg))
        }

        publishHeartbeatEvents(workflow)
        HttpResponse.ok(new TraceAliveResponse(message: 'OK'))
    }


    @Post("/workflow")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceWorkflowResponse> workflow(@Body TraceWorkflowRequest request, Authentication authentication) {
        HttpResponse<TraceWorkflowResponse> response
        try {
            User user = userService.getFromAuthData(authentication)
            log.info("Receiving workflow trace for workflows ID=${request.workflow?.id}")
            Workflow workflow = traceService.processWorkflowTrace(request, user)
            final resp = new TraceWorkflowResponse(
                    status: TraceProcessingStatus.OK,
                    workflowId: workflow.id,
                    watchUrl: "${serverUrl}/watch/${workflow.id}"
            )
            response = HttpResponse.created(resp)

            publishHeartbeatEvents(workflow)
            publishWorkflowEvent(workflow, user)
        }
        catch (Exception e) {
            log.error("Failed to handle workflow trace=${request.workflow?.id}", e)
            response = HttpResponse.badRequest(TraceWorkflowResponse.ofError(e.message))
        }

        response
    }

    @Post("/task")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceTaskResponse> task(@Body TraceTaskRequest request) {
        HttpResponse<TraceTaskResponse> response
        if( !request.workflowId )
            HttpResponse.badRequest(TraceTaskResponse.ofError("Missing workflow ID"))

        try {
            log.info("Receiving task trace for workflow ID=${request.workflowId}")
            List<Task> tasks = traceService.processTaskTrace(request)

            Workflow workflow = tasks.first().workflow
            publishHeartbeatEvents(workflow)
            response = HttpResponse.created(TraceTaskResponse.ofSuccess(workflow.id))
            publishProgressEvent(workflow)
        }
        catch (Exception e) {
            log.error("Failed to handle tasks trace for request: $request", e)
            response = HttpResponse.badRequest(TraceTaskResponse.ofError(e.message))
        }

        response
    }

    @Get("/hello")
    @Secured(['ROLE_USER'])
    HttpResponse<TraceHelloResponse> hello(Authentication authentication) {
        log.info "Trace hello from ${authentication.getName()}"
        HttpResponse.ok(new TraceHelloResponse(message: 'Want to play again?'))
    }

}

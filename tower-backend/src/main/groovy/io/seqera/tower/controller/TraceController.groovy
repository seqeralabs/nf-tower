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
import java.time.Duration

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
import io.reactivex.Flowable
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.SseErrorType
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.trace.TraceAliveRequest
import io.seqera.tower.exchange.trace.TraceAliveResponse
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
import org.reactivestreams.Publisher
/**
 * Implements the `trace` API
 *
 */
@Controller("/trace")
@Secured(SecurityRule.IS_ANONYMOUS)
@Slf4j
class TraceController extends BaseController {


    @Value('${sse.time.idle.workflow:5m}')
    Duration idleWorkflowFlowableTimeout
    @Value('${sse.time.throttle.workflow:1s}')
    Duration throttleWorkflowFlowableTime

    @Value('${sse.time.idle.user:5m}')
    Duration idleUserFlowableTimeout
    @Value('${sse.time.throttle.user:1h}')
    Duration throttleUserFlowableTime
    @Value('${sse.time.heartbeat.user:1m}')
    Duration heartbeatUserFlowableInterval


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

    private static String getWorkflowFlowableKey(String workflowId) {
        return "workflow-${workflowId}"
    }

    private static String getUserFlowableKey(def userId) {
        return "user-${userId}"
    }

    protected void publishHeartbeatEvents(Workflow workflow) {
        String workflowFlowableKey = getWorkflowFlowableKey(workflow.id)
        String userFlowableKey = getUserFlowableKey(workflow.ownerId)

        if (workflow.checkIsStarted()) {
            log.info("Client heartbeats received for flowables: ${workflowFlowableKey} and ${userFlowableKey}")
            serverSentEventsService.tryPublish(workflowFlowableKey) {
                Event.of(TraceSseResponse.ofHeartbeat("Client heartbeat [${workflowFlowableKey}]"))
            }
            serverSentEventsService.tryPublish(userFlowableKey) {
                Event.of(TraceSseResponse.ofHeartbeat("Client heartbeat [${userFlowableKey}]"))
            }
        }
    }

    private void publishWorkflowEvent(Workflow workflow, User user) {
        String userFlowableKey = getUserFlowableKey(user.id)

        serverSentEventsService.tryPublish(userFlowableKey) {
            Event.of(TraceSseResponse.ofWorkflow(WorkflowGet.of(workflow)))
        }

        if (!workflow.checkIsStarted()) {
            String workflowFlowableKey = getWorkflowFlowableKey(workflow.id)
            serverSentEventsService.tryComplete(workflowFlowableKey)
        }
    }

    private void publishProgressEvent(Workflow workflow) {
        String workflowFlowableKey = getWorkflowFlowableKey(workflow.id)

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
            response = HttpResponse.created(TraceWorkflowResponse.ofSuccess(workflow.id))

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
            log.error("Failed to handle trace trace=$request", e)
            response = HttpResponse.badRequest(TraceTaskResponse.ofError(e.message))
        }

        response
    }

    @Get("/live/workflow/{workflowId}")
    Publisher<Event<TraceSseResponse>> liveWorkflow(String workflowId) {
        String workflowFlowableKey = getWorkflowFlowableKey(workflowId)

        log.info("Subscribing to live events of workflow: ${workflowFlowableKey}")
        Flowable<Event<TraceSseResponse>> workflowFlowable
        try {
            workflowFlowable = serverSentEventsService.getOrCreate(workflowFlowableKey, idleWorkflowFlowableTimeout, null)
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter: ${workflowFlowableKey}"
            log.error("${message} | ${e.message}", e)
            workflowFlowable = Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        return workflowFlowable
    }

    @Get("/live/user/{userId}")
    Publisher<Event<TraceSseResponse>> liveUser(Long userId) {
        String userFlowableKey = getUserFlowableKey(userId)

        log.info("Subscribing to live events of user: ${userFlowableKey}")
        Flowable<Event<TraceSseResponse>> userFlowable
        try {
            userFlowable = serverSentEventsService.getOrCreate(userFlowableKey, idleUserFlowableTimeout, null)
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter: ${userFlowableKey}"
            log.error("${message} | ${e.message}", e)

            return Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        Flowable heartbeatUserFlowable = serverSentEventsService.generateHeartbeatFlowable(heartbeatUserFlowableInterval, {
            log.info("Server heartbeat ${it} generated for flowable: ${userFlowableKey}")
            Event.of(TraceSseResponse.ofHeartbeat("Server heartbeat [${userFlowableKey}]"))
        })

        return userFlowable.mergeWith(heartbeatUserFlowable)
                           .takeUntil(userFlowable.takeLast(1))
    }

}

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


    @Value('${sse.time.idle.workflow-detail:5m}')
    Duration idleWorkflowDetailFlowableTimeout
    @Value('${sse.time.throttle.workflow-detail:1s}')
    Duration throttleWorkflowDetailFlowableTime

    @Value('${sse.time.idle.workflow-list:5m}')
    Duration idleWorkflowListFlowableTimeout
    @Value('${sse.time.throttle.workflow-list:1h}')
    Duration throttleWorkflowListFlowableTime
    @Value('${sse.time.heartbeat.workflow-list:1m}')
    Duration heartbeatWorkflowListFlowableInterval


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
            publishWorkflowEvent(workflow, user)
        }
        catch (Exception e) {
            log.error("Failed to handle workflow trace=${request.workflow?.id}", e)
            response = HttpResponse.badRequest(TraceWorkflowResponse.ofError(e.message))
        }

        response
    }

    private void publishWorkflowEvent(Workflow workflow, User user) {
        final workflowListFlowableKey = getWorkflowListFlowableKey(user.id)

        serverSentEventsService.tryPublish(workflowListFlowableKey) {
            Event.of(TraceSseResponse.ofWorkflow(WorkflowGet.of(workflow)))
        }

        if (!workflow.checkIsStarted()) {
            final workflowDetailFlowableKey = getWorkflowDetailFlowableKey(workflow.id)
            serverSentEventsService.completeFlowable(workflowDetailFlowableKey)
        }
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

            final workflow = tasks.first().workflow
            response = HttpResponse.created(TraceTaskResponse.ofSuccess(workflow.id.toString()))
            publishProgressEvent(workflow)
        }
        catch (Exception e) {
            log.error("Failed to handle trace trace=$request", e)
            response = HttpResponse.badRequest(TraceTaskResponse.ofError(e.message))
        }

        response
    }

    private void publishProgressEvent(Workflow workflow) {
        String workflowDetailFlowableKey = getWorkflowDetailFlowableKey(workflow.id)

        serverSentEventsService.tryPublish(workflowDetailFlowableKey) {
            ProgressData progress = progressService.fetchWorkflowProgress(workflow)
            Event.of(TraceSseResponse.ofProgress(progress))
        }
    }

    @Get("/live/workflowDetail/{workflowId}")
    Publisher<Event<TraceSseResponse>> liveWorkflowDetail(String workflowId) {
        String workflowDetailFlowableKey = getWorkflowDetailFlowableKey(workflowId)

        log.info("Subscribing to live events of workflow: ${workflowDetailFlowableKey}")
        Flowable<Event<TraceSseResponse>> workflowDetailFlowable
        try {
            workflowDetailFlowable = serverSentEventsService.getOrCreate(workflowDetailFlowableKey)
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter: ${workflowDetailFlowableKey}"
            log.error("${message} | ${e.message}", e)
            workflowDetailFlowable = Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        return workflowDetailFlowable.doOnSubscribe({
            log.info("*** Subscribed flowable: ${workflowDetailFlowableKey}")
        }).doOnCancel({
            log.info("*** Cancelled flowable: ${workflowDetailFlowableKey}")
        })
    }

    private static String getWorkflowDetailFlowableKey(String workflowId) {
        return "workflow-${workflowId}"
    }

    @Get("/live/workflowList/{userId}")
    Publisher<Event<TraceSseResponse>> liveWorkflowList(Long userId) {
        String workflowListFlowableKey = getWorkflowListFlowableKey(userId)

        log.info("Subscribing to live events of user: ${workflowListFlowableKey}")
        Flowable<Event<TraceSseResponse>> workflowListFlowable
        try {
            workflowListFlowable = serverSentEventsService.getOrCreate(workflowListFlowableKey)
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter: ${workflowListFlowableKey}"
            log.error("${message} | ${e.message}", e)

            return Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        Flowable heartbeatWorkflowListFlowable = serverSentEventsService.generateHeartbeatFlowable(heartbeatWorkflowListFlowableInterval, {
            log.info("Generating heartbeat ${it} for ${workflowListFlowableKey}")
            Event.of(TraceSseResponse.ofHeartbeat("Heartbeat ${it}"))
        })

        return workflowListFlowable.mergeWith(heartbeatWorkflowListFlowable)
                                   .takeUntil(workflowListFlowable.takeLast(1))
                                   .doOnSubscribe({
                                       log.info("+++ Subscribed flowable: ${workflowListFlowableKey}")
                                   })
                                   .doOnCancel({
                                       log.info("+++ Cancelled flowable: ${workflowListFlowableKey}")
                                   })
    }

    private static String getWorkflowListFlowableKey(def userId) {
        return "user-${userId}"
    }

}

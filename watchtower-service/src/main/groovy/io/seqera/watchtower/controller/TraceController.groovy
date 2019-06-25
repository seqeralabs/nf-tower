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

package io.seqera.watchtower.controller

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
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.SseErrorType
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException
import io.seqera.watchtower.pogo.exchange.progress.ProgressGet
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse
import io.seqera.watchtower.pogo.exchange.trace.sse.TraceSseResponse
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet
import io.seqera.watchtower.service.ProgressService
import io.seqera.watchtower.service.ServerSentEventsService
import io.seqera.watchtower.service.TraceService
import io.seqera.watchtower.service.UserService
import org.reactivestreams.Publisher

import javax.inject.Inject
import java.time.Duration

/**
 * Implements the `trace` API
 *
 */
@Controller("/trace")
@Secured(SecurityRule.IS_ANONYMOUS)
@Slf4j
class TraceController {


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
    HttpResponse<TraceWorkflowResponse> workflow(@Body TraceWorkflowRequest trace, Authentication authentication) {
        HttpResponse<TraceWorkflowResponse> response
        try {
            User user = userService.getFromAuthData(authentication)
            log.info("Receiving workflow trace: ${trace.inspect()}")
            Workflow workflow = traceService.processWorkflowTrace(trace, user)
            log.info("Processed workflow trace ${workflow.id}")

            response = HttpResponse.created(TraceWorkflowResponse.ofSuccess(workflow.id.toString()))

            publishWorkflowEvent(workflow, user)
        } catch (Exception e) {
            log.error("Failed to handle workflow trace=$trace", e)
            response = HttpResponse.badRequest(TraceWorkflowResponse.ofError(e.message))
        }

        response
    }

    private void publishWorkflowEvent(Workflow workflow, User user) {
        String workflowDetailFlowableKey = getWorkflowDetailFlowableKey(workflow.id)
        String workflowListFlowableKey = getWorkflowListFlowableKey(user.id)

        if (workflow.checkIsStarted()) {
            serverSentEventsService.createFlowable(workflowDetailFlowableKey, idleWorkflowDetailFlowableTimeout)
        } else {

            WorkflowGet workflowGet = progressService.buildWorkflowGet(workflow)
            try {
                serverSentEventsService.publishEvent(workflowDetailFlowableKey, Event.of(TraceSseResponse.ofWorkflow(workflowGet)))
            } catch (NonExistingFlowableException e) {
                log.error("No flowable found while trying to publish workflow data: ${workflowDetailFlowableKey}")
            }

        }

        try {
            serverSentEventsService.publishEvent(workflowListFlowableKey, Event.of(TraceSseResponse.ofWorkflow(WorkflowGet.of(workflow))))
        } catch (NonExistingFlowableException e) {
            log.error("No flowable found while trying to publish workflow data: ${workflowListFlowableKey}")
        }

        if (!workflow.checkIsStarted()) {
            serverSentEventsService.completeFlowable(workflowDetailFlowableKey)
        }
    }

    @Post("/task")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceTaskResponse> task(@Body TraceTaskRequest trace) {
        HttpResponse<TraceTaskResponse> response
        try {
            log.info("Receiving task trace: ${trace.inspect()}")
            List<Task> tasks = traceService.processTaskTrace(trace)
            log.info("Processed task trace ${tasks.id} (${tasks.taskId} ${tasks.status*.name()})")

            response = HttpResponse.created(TraceTaskResponse.ofSuccess(trace.workflowId.toString()))
            publishUpdatedProgressEvent((Long) tasks.first().workflowId)
        } catch (Exception e) {
            log.error("Failed to handle trace trace=$trace", e)
            response = HttpResponse.badRequest(TraceTaskResponse.ofError(e.message))
        }

        response
    }

    private void publishUpdatedProgressEvent(Long workflowId) {
        String workflowDetailFlowableKey = getWorkflowDetailFlowableKey(workflowId)

        try {
            ProgressGet progress = progressService.computeWorkflowProgress(workflowId)
            serverSentEventsService.publishEvent(workflowDetailFlowableKey, Event.of(TraceSseResponse.ofProgress(progress)))
        } catch (NonExistingFlowableException e) {
            log.error("No flowable found while trying to publish task data: ${workflowDetailFlowableKey}")
        }
    }

    @Get("/live/workflowDetail/{workflowId}")
    Publisher<Event<TraceSseResponse>> liveWorkflowDetail(Long workflowId) {
        String workflowDetailFlowableKey = getWorkflowDetailFlowableKey(workflowId)

        log.info("Subscribing to live events of workflow: ${workflowDetailFlowableKey}")
        Flowable<Event<TraceSseResponse>> flowable
        try {
            flowable = serverSentEventsService.getThrottledFlowable(workflowDetailFlowableKey, throttleWorkflowDetailFlowableTime)
        } catch (NonExistingFlowableException e) {
            String message = "No live events emitter: ${workflowDetailFlowableKey}"
            log.info(message)
            flowable = Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.NONEXISTENT, message)))
        } catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter: ${workflowDetailFlowableKey}"
            log.error("${message} | ${e.message}", e)
            flowable = Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        flowable
    }

    private static String getWorkflowDetailFlowableKey(def workflowId) {
        return "workflow-${workflowId}"
    }

    @Get("/live/workflowList/{userId}")
    Publisher<Event<TraceSseResponse>> liveWorkflowList(Long userId) {
        String workflowListFlowableKey = getWorkflowListFlowableKey(userId)

        log.info("Subscribing to live events of user: ${workflowListFlowableKey}")
        Flowable<Event<TraceSseResponse>> flowable
        try {
            flowable = serverSentEventsService.getThrottledFlowable(workflowListFlowableKey, throttleWorkflowListFlowableTime)
        } catch (NonExistingFlowableException e) {
            String message = "No live events emitter. Generating one: ${workflowListFlowableKey}"
            log.info(message)

            serverSentEventsService.createFlowable(workflowListFlowableKey, idleWorkflowListFlowableTimeout)
            flowable = serverSentEventsService.getThrottledFlowable(workflowListFlowableKey, throttleWorkflowListFlowableTime)
        } catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter: ${workflowListFlowableKey}"
            log.error("${message} | ${e.message}", e)

            return Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        return flowable.mergeWith(
            serverSentEventsService.generateHeartbeatFlowable(
                heartbeatWorkflowListFlowableInterval,
                    {
                        log.info("Generating heartbeat ${it} for ${workflowListFlowableKey}")
                        Event.of(TraceSseResponse.ofHeartbeat("Heartbeat ${it}"))
                    }
                )
        )
    }

    private static String getWorkflowListFlowableKey(def userId) {
        return "user-${userId}"
    }

}
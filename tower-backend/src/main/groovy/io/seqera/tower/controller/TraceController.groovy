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

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.retry.annotation.Retryable
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.seqera.tower.domain.HashSequenceGenerator
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowKey
import io.seqera.tower.enums.LiveAction
import io.seqera.tower.enums.TraceProcessingStatus
import io.seqera.tower.exchange.live.LiveUpdate
import io.seqera.tower.exchange.trace.TraceAliveRequest
import io.seqera.tower.exchange.trace.TraceAliveResponse
import io.seqera.tower.exchange.trace.TraceInitRequest
import io.seqera.tower.exchange.trace.TraceInitResponse
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceTaskResponse
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.exchange.trace.TraceWorkflowResponse
import io.seqera.tower.service.LiveEventsService
import io.seqera.tower.service.ProgressService
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
    LiveEventsService serverSentEventsService
    @Inject TransactionService transactionService

    @Inject
    TraceController(TraceService traceService, ProgressService progressService, UserService userService, LiveEventsService serverSentEventsService) {
        this.traceService = traceService
        this.progressService = progressService
        this.userService = userService
        this.serverSentEventsService = serverSentEventsService
    }


    protected void publishWorkflowEvent(Workflow workflow, User user) {
        serverSentEventsService.publishEvent(LiveUpdate.of(user.id, workflow.id, LiveAction.WORKFLOW_UPDATE))
    }

    protected void publishProgressEvent(Workflow workflow) {
        serverSentEventsService.publishEvent(LiveUpdate.of(workflow.owner.id, workflow.id, LiveAction.PROGRESS_UPDATE))
    }

    @Post("/alive")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceAliveResponse> alive(@Body TraceAliveRequest req, Authentication authentication) {
        log.info "Receiving trace alive [workflowId=${req.workflowId}; user=${authentication.name}]"

        Workflow workflow = Workflow.get(req.workflowId)
        if (!workflow) {
            final msg = "Cannot find workflowId=$req.workflowId"
            log.warn(msg)
            return HttpResponse.badRequest(new TraceAliveResponse(message:msg))
        }

        HttpResponse.ok(new TraceAliveResponse(message: 'OK'))
    }


    @Post("/workflow")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceWorkflowResponse> workflow(@Body TraceWorkflowRequest req, Authentication authentication) {
        try {
            final msg = (req.workflow.checkIsStarted()
                    ? "Receiving trace for new workflow [user=${authentication.name}]"
                    : "Receiving trace for workflow completion [workflowId=${req.workflow.id}; user=${authentication.name}]")
            log.info(msg)

            User user = userService.getFromAuthData(authentication)
            Workflow workflow = traceService.processWorkflowTrace(req, user)

            final resp = new TraceWorkflowResponse(
                    status: TraceProcessingStatus.OK,
                    workflowId: workflow.id,
                    watchUrl: "${serverUrl}/watch/${workflow.id}"
            )
            publishWorkflowEvent(workflow, user)
            return HttpResponse.created(resp)
        }
        catch (Exception e) {
            log.error("Failed to handle workflow trace=${req.workflow?.id}", e)
            return HttpResponse.badRequest(TraceWorkflowResponse.ofError(e.message))
        }
    }

    @Post("/task")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceTaskResponse> task(@Body TraceTaskRequest req, Authentication authentication) {
        log.info "Receiving task trace request [workflowId=${req.workflowId}; user=${authentication.name}]"

        HttpResponse<TraceTaskResponse> response
        if( !req.workflowId )
            HttpResponse.badRequest(TraceTaskResponse.ofError("Missing workflow ID"))

        try {
            List<Task> tasks = traceService.processTaskTrace(req)

            Workflow workflow = tasks.first().workflow
            progressService.updateLoadPeaks(workflow) // this query should be cached into 2nd level cache
            response = HttpResponse.created(TraceTaskResponse.ofSuccess(workflow.id))
            publishProgressEvent(workflow)
        }
        catch (Exception e) {
            log.error("Failed to handle tasks trace for request: $req", e)
            response = HttpResponse.badRequest(TraceTaskResponse.ofError(e.message))
        }

        response
    }

    @Post("/init")
    @Secured(['ROLE_USER'])
    HttpResponse<TraceInitResponse> init(TraceInitRequest req, Authentication authentication) {
        log.info "Receiving trace init [user=${authentication.getName()}]"
        final key = transactionService.withTransaction {
            def record = new WorkflowKey()
            record.sessionId = req.sessionId
            record.save()
        }
        final workflowId = HashSequenceGenerator.getHash(key.id)
        final resp = new TraceInitResponse(workflowId: workflowId, message: 'OK')
        HttpResponse.ok(resp)
    }

    @Get("/ping")
    @Secured(SecurityRule.IS_ANONYMOUS)
    HttpResponse<String> ping(HttpRequest req) {
        log.info "Trace ping from ${req.remoteAddress}"
        HttpResponse.ok('pong')
    }
}

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
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.seqera.tower.domain.Task
import io.seqera.tower.enums.TraceProcessingStatus
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

    @Inject TraceService traceService
    @Inject ProgressService progressService
    @Inject UserService userService
    @Inject LiveEventsService liveEventsService

    @Post("/alive")
    @Transactional(readOnly = true)
    @Secured(['ROLE_USER'])
    HttpResponse<TraceAliveResponse> alive(@Body TraceAliveRequest req, Authentication authentication) {
        log.info "Receiving trace alive [workflowId=${req.workflowId}; user=${authentication.name}]"
        traceService.keepAlive(req.workflowId)
        HttpResponse.ok(new TraceAliveResponse(message: 'OK'))
    }


    @Post("/workflow")
    @Transactional
    @Secured(['ROLE_USER'])
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
    @Secured(['ROLE_USER'])
    HttpResponse<TraceTaskResponse> task(@Body TraceTaskRequest req, Authentication authentication) {
        log.info "Receiving task trace request [workflowId=${req.workflowId}; tasks=${req.tasks?.size()}; user=${authentication.name}]"

        HttpResponse<TraceTaskResponse> response
        if( !req.workflowId )
            HttpResponse.badRequest(TraceTaskResponse.ofError("Missing workflow ID"))

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
    @Secured(['ROLE_USER'])
    HttpResponse<TraceInitResponse> init(TraceInitRequest req, Authentication authentication) {
        log.info "Receiving trace init [user=${authentication.getName()}]"
        final workflowId = traceService.createWorkflowKey()
        final resp = new TraceInitResponse(workflowId: workflowId, message: 'OK')
        log.info "Created new workflow ID=${workflowId} [user=${authentication.getName()}]"
        HttpResponse.ok(resp)
    }

    @Get("/ping")
    @Secured(SecurityRule.IS_ANONYMOUS)
    HttpResponse<String> ping(HttpRequest req) {
        log.info "Trace ping from ${req.remoteAddress}"
        HttpResponse.ok('pong')
    }
}

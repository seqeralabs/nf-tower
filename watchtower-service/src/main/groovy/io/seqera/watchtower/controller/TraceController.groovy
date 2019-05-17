package io.seqera.watchtower.controller


import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse
import io.seqera.watchtower.service.TraceService

import javax.inject.Inject

/**
 * Implements the `trace` API
 *
 */
@Controller("/trace")
@Slf4j
class TraceController {

    TraceService traceService

    @Inject
    TraceController(TraceService traceService) {
        this.traceService = traceService
    }


    @Post("/workflow")
    HttpResponse<TraceWorkflowResponse> workflow(@Body TraceWorkflowRequest trace) {
        log.info("Receiving workflow trace: ${trace.inspect()}")
        TraceWorkflowResponse traceResponse = traceService.processWorkflowTrace(trace)
        log.info("Processed workflow trace: ${trace.inspect()}")

        HttpResponse<TraceWorkflowResponse> response
        if (traceResponse.message) {
            response = HttpResponse.badRequest(traceResponse)
        } else {
            response = HttpResponse.created(traceResponse)
        }

        response
    }

    @Post("/task")
    HttpResponse<TraceTaskResponse> task(@Body TraceTaskRequest trace) {
        log.info("Receiving task trace: ${trace.inspect()}")
        TraceTaskResponse traceResponse = traceService.processTaskTrace(trace)
        log.info("Processed task trace: ${trace.inspect()}")

        HttpResponse<TraceTaskResponse> response
        if (traceResponse.message) {
            response = HttpResponse.badRequest(traceResponse)
        } else {
            response = HttpResponse.created(traceResponse)
        }

        response
    }

}
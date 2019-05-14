package io.seqera.watchtower.controller

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
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


    @Get("/")
    HttpStatus index() {
        return HttpStatus.OK
    }


    @Post("/save")
    HttpResponse<TraceWorkflowResponse> save(@Body TraceWorkflowRequest trace) {
        log.info("Receiving trace: ${trace.inspect()}")
        TraceWorkflowResponse traceResponse = traceService.createEntityByTrace(trace)
        log.info("Processed trace: ${trace.inspect()}")

        HttpResponse<TraceWorkflowResponse> response
        if (traceResponse.message) {
            response = HttpResponse.badRequest(traceResponse)
        } else {
            response = HttpResponse.created(traceResponse)
        }

        response
    }

}
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

@Controller("/trace")
@Slf4j
class TraceController {

    @Inject
    TraceService traceService

    @Get("/")
    HttpStatus index() {
        return HttpStatus.OK
    }


    @Post("/save")
    HttpResponse<String> save(@Body Map json) {
        log.info("Receiving trace: ${json.inspect()}")
        Map result = traceService.createEntityByTrace(json)
        log.info("Processed trace: ${result.inspect()}")
        String jsonResult = new ObjectMapper().writeValueAsString(result)

        HttpResponse<String> response
        if (result.error) {
            response = HttpResponse.badRequest(jsonResult)
        } else {
            response = HttpResponse.created(jsonResult)
        }

        response.contentType(MediaType.APPLICATION_JSON)
    }

}
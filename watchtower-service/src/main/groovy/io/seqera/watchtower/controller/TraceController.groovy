package io.seqera.watchtower.controller

import com.fasterxml.jackson.databind.ObjectMapper
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.seqera.watchtower.service.TraceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

@Controller("/trace")
@CompileStatic
class TraceController {

    private static final Logger LOG = LoggerFactory.getLogger(TraceService.class);

    @Inject
    TraceService traceService

    @Get("/")
    HttpStatus index() {
        return HttpStatus.OK
    }


    @Post("/save")
    HttpResponse<String> save(@Body Map json) {
        LOG.info("Receiving trace: ${json.inspect()}")
        Map result = traceService.createEntityByTrace(json)
        LOG.info("Processed trace: ${result.inspect()}")
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
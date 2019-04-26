package watchtower.service.controller

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Post
import watchtower.service.domain.Workflow
import watchtower.service.service.TraceService
import watchtower.service.service.WorkflowService

import javax.inject.Inject
import java.time.Instant


@Controller("/trace")
@CompileStatic
class TraceController {

    @Inject
    TraceService traceService

    @Get("/")
    HttpStatus index() {
        return HttpStatus.OK
    }


    @Post("/save")
    HttpResponse<String> save(@Body Map json) {
        Map result = traceService.createEntityByTrace(json)

        String jsonResult = new ObjectMapper().writeValueAsString(result)

        HttpResponse.created(jsonResult)
                    .contentType(MediaType.APPLICATION_JSON)
    }

}
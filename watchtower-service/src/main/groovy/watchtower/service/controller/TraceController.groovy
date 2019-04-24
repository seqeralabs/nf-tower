package watchtower.service.controller

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import watchtower.service.domain.Workflow
import watchtower.service.service.WorkflowService


@Controller("/trace")
class TraceController {

    WorkflowService workflowService

    TraceController(WorkflowService workflowService) {
        this.workflowService = workflowService
    }

    @Get("/")
    HttpStatus index() {
        return HttpStatus.OK
    }

}
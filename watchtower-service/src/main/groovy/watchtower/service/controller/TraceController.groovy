package watchtower.service.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Post
import watchtower.service.domain.Workflow
import watchtower.service.service.WorkflowService

import java.time.Instant


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


    @Get("/save")
    Long save() {
        Workflow workflow = new Workflow(runId: UUID.randomUUID().toString(), runName: 'name', utcTime: Instant.now()).save()
        return workflow.id
    }

}
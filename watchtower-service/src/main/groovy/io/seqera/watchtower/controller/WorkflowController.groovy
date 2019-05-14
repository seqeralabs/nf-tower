package io.seqera.watchtower.controller


import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.marshaller.WorkflowTraceJsonMarshaller
import io.seqera.watchtower.service.WorkflowService

import javax.inject.Inject
import java.time.Instant

/**
 * Implements the `workflow` API
 */
@Controller("/workflow")
@Slf4j
class WorkflowController {

    WorkflowService workflowService

    @Inject
    WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService
    }


    @Get("/list")
    @Transactional
    HttpResponse<List<TraceWorkflowRequest>> list() {
        List<Workflow> workflows = workflowService.list()

        List<TraceWorkflowRequest> result = workflows.collect {
            new TraceWorkflowRequest(workflow: it, utcTime: Instant.now())
        }

        HttpResponse.ok(result)
    }

    @Get("/{id}")
    @Transactional
    HttpResponse<TraceWorkflowRequest> get(Long id) {
        Workflow workflow = workflowService.get(id)

        if (!workflow) {
            return HttpResponse.notFound()
        }

        HttpResponse.ok(new TraceWorkflowRequest(workflow: workflow, utcTime: Instant.now()))
    }


}

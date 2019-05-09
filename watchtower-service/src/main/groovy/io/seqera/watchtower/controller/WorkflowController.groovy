package io.seqera.watchtower.controller


import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.marshaller.WorkflowJsonMarshaller
import io.seqera.watchtower.service.WorkflowService

import javax.inject.Inject

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
    HttpResponse<List> list() {
        Workflow workflow = workflowService.get(1)

        if (!workflow) {
            return HttpResponse.notFound()
        }

        List json = [WorkflowJsonMarshaller.generateJson(workflow)]
        HttpResponse.ok(json)
    }

    @Get("/{id}")
    @Transactional
    HttpResponse<Map> get(Long id) {
        Workflow workflow = workflowService.get(id)

        if (!workflow) {
            return HttpResponse.notFound()
        }

        Map json = WorkflowJsonMarshaller.generateJson(workflow)
        HttpResponse.ok(json)
    }


}

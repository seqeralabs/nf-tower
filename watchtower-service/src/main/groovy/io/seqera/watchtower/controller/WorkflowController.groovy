package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowList
import io.seqera.watchtower.service.WorkflowService

import javax.inject.Inject

/**
 * Implements the `workflow` API
 */
@Controller("/workflow")
@Secured(['ROLE_USER'])
@Slf4j
class WorkflowController {

    WorkflowService workflowService

    @Inject
    WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService
    }


    @Get("/list")
    @Transactional
    HttpResponse<WorkflowList> list() {
        List<Workflow> workflows = workflowService.list()

        List<WorkflowGet> result = workflows.collect {
            buildWorkflowGetResponse(it)
        }
        HttpResponse.ok(new WorkflowList(workflows: result))
    }

    @Get("/{id}")
    @Transactional
    HttpResponse<WorkflowGet> get(Long id) {
        Workflow workflow = workflowService.get(id)

        if (!workflow) {
            return HttpResponse.notFound()
        }
        HttpResponse.ok(buildWorkflowGetResponse(workflow))
    }

    private static WorkflowGet buildWorkflowGetResponse(Workflow workflow) {
        new WorkflowGet(workflow: workflow, summary: workflow.summaryEntries as List, progress: workflow.progress)
    }


}

/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.controller

import javax.inject.Inject

import grails.gorm.PagedResultList
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpParameters
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.exchange.MessageResponse
import io.seqera.tower.exchange.task.TaskGet
import io.seqera.tower.exchange.task.TaskList
import io.seqera.tower.exchange.workflow.GetWorkflowMetricsResponse
import io.seqera.tower.exchange.workflow.WorkflowGet
import io.seqera.tower.exchange.workflow.WorkflowList
import io.seqera.tower.service.ProgressService
import io.seqera.tower.service.TaskService
import io.seqera.tower.service.UserService
import io.seqera.tower.service.WorkflowService
/**
 * Implements the `workflow` API
 */
@Controller("/workflow")
@Slf4j
class WorkflowController {

    WorkflowService workflowService
    TaskService taskService
    ProgressService progressService
    UserService userService

    @Inject
    WorkflowController(WorkflowService workflowService, TaskService taskService, ProgressService progressService, UserService userService) {
        this.workflowService = workflowService
        this.taskService = taskService
        this.progressService = progressService
        this.userService = userService
    }


    @Get("/list")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<WorkflowList> list(Authentication authentication) {
        List<Workflow> workflows = workflowService.listByOwner(userService.getFromAuthData(authentication))

        List<WorkflowGet> result = workflows.collect { Workflow workflow ->
            WorkflowGet.of(workflow)
        }
        HttpResponse.ok(WorkflowList.of(result))
    }

    @Get("/{id}")
    @Transactional
    @Secured(SecurityRule.IS_ANONYMOUS)
    HttpResponse<WorkflowGet> get(Long id) {
        Workflow workflow = workflowService.get(id)

        if (!workflow) {
            return HttpResponse.notFound()
        }
        HttpResponse.ok(progressService.buildWorkflowGet(workflow))
    }

    @Get("/{workflowId}/tasks")
    @Transactional
    @Secured(SecurityRule.IS_ANONYMOUS)
    HttpResponse<TaskList> tasks(Long workflowId, HttpParameters filterParams) {
        Long max = filterParams.getFirst('length', Long.class, 10l)
        Long offset = filterParams.getFirst('start', Long.class, 0l)
        String orderProperty = filterParams.getFirst('order[0][column]', String.class, 'taskId')
        String orderDir = filterParams.getFirst('order[0][dir]', String.class, 'asc')

        String search = filterParams.getFirst('search', String.class, '')
        String searchRegex = search.contains('*') ? search.replaceAll(/\*/, '%') : "${search}%"

        PagedResultList<Task> taskPagedResultList = taskService.findTasks(workflowId, max, offset, orderProperty, orderDir, searchRegex)

        List<TaskGet> result = taskPagedResultList.collect {
            TaskGet.of(it)
        }
        HttpResponse.ok(TaskList.of(result, taskPagedResultList.totalCount))
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Delete('/delete/{workflowId}')
    HttpResponse delete(Serializable workflowId) {
        try {
            workflowService.deleteById(workflowId)
            HttpResponse.status(HttpStatus.NO_CONTENT)
        }
        catch( Exception e ) {
            log.error "Unable to delete workflow with id=$workflowId", e
            HttpResponse.badRequest(new MessageResponse("Oops... Failed to delete workflow with ID $workflowId"))
        }
    }

    @Transactional
    @Secured(['ROLE_USER'])
    @Get('/metrics/{workflowId}')
    @CompileDynamic
    HttpResponse<GetWorkflowMetricsResponse> metrics(Serializable workflowId) {
        try {
            final workflow = workflowService.get(workflowId)
            if (!workflow)
                return HttpResponse.notFound(new GetWorkflowMetricsResponse(message:"Oops... Can't find workflow ID $workflowId"))

            final result = workflowService.findMetrics(workflow)
            HttpResponse.ok(new GetWorkflowMetricsResponse(metrics: new ArrayList<WorkflowMetrics>(result)))
        }
        catch( Exception e ) {
            log.error "Unable to delete workflow with id=$workflowId", e
            HttpResponse.badRequest(new GetWorkflowMetricsResponse(message:"Oops... Failed to get execution metrics for workflow ID $workflowId"))
        }
    }

}

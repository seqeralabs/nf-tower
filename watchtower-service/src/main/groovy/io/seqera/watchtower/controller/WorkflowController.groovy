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

package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpParameters
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exchange.task.TaskGet
import io.seqera.watchtower.pogo.exchange.task.TaskList
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowList
import io.seqera.watchtower.service.TaskService
import io.seqera.watchtower.service.UserService
import io.seqera.watchtower.service.WorkflowService

import javax.inject.Inject

/**
 * Implements the `workflow` API
 */
@Controller("/workflow")
@Slf4j
class WorkflowController {

    WorkflowService workflowService
    TaskService taskService
    UserService userService

    @Inject
    WorkflowController(WorkflowService workflowService, TaskService taskService, UserService userService) {
        this.workflowService = workflowService
        this.taskService = taskService
        this.userService = userService
    }


    @Get("/list")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<WorkflowList> list(Authentication authentication) {
        List<Workflow> workflows = workflowService.list(userService.getFromAuthData(authentication))

        List<WorkflowGet> result = workflows.collect { Workflow workflow ->
            new WorkflowGet(workflow)
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
        HttpResponse.ok(WorkflowGet.of(workflow))
    }

    @Get("/{workflowId}/tasks")
    @Transactional
    @Secured(SecurityRule.IS_ANONYMOUS)
    HttpResponse<TaskList> tasks(Long workflowId, HttpParameters filterParams) {
        Long max = filterParams.getFirst('start', Long.class, 10000l)
        Long offset = filterParams.getFirst('length', Long.class, 0l)

        List<TaskGet> result = taskService.findTasks(workflowId, max, offset).collect {
            TaskGet.of(it)
        }
        HttpResponse.ok(TaskList.of(result))
    }

}

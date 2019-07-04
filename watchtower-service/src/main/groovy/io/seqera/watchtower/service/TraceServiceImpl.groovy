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

package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import org.springframework.validation.FieldError

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException

@Singleton
@Transactional
class TraceServiceImpl implements TraceService {

    WorkflowService workflowService

    TaskService taskService

    @Inject
    TraceServiceImpl(WorkflowService workflowService, TaskService taskService) {
        this.workflowService = workflowService
        this.taskService = taskService
    }


    Workflow processWorkflowTrace(TraceWorkflowRequest request, User owner) {
        Workflow workflow = workflowService.processTraceWorkflowRequest(request, owner)
        checkWorkflowSaveErrors(workflow)

        workflow
    }

    List<Task> processTaskTrace(TraceTaskRequest request) {
        List<Task> tasks = taskService.processTaskTraceRequest(request)
        tasks.each { Task task ->
            checkTaskSaveErrors(task)
        }

        return tasks
    }

    private void checkWorkflowSaveErrors(Workflow workflow) {
        if (!workflow.hasErrors()) {
            return
        }

        List<FieldError> fieldErrors = workflow.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            throw new ValidationException("Can't save a workflow without ${nullableError.field}")
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            throw new ValidationException("Can't save a workflow with the same ${uniqueError.field} of another")
        }

        List<String> uncustomizedErrors = fieldErrors.collect { "${it.field}|${it.code}".toString() }
        throw new ValidationException("Can't save task. Validation errors: ${uncustomizedErrors}")
    }

    private void checkTaskSaveErrors(Task task) {
        if (!task.hasErrors()) {
            return
        }

        List<FieldError> fieldErrors = task.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            throw new ValidationException("Can't save a task without ${nullableError.field}")
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            throw new ValidationException("Can't save a task with the same ${uniqueError.field} of another")
        }

        List<String> uncustomizedErrors = fieldErrors.collect { "${it.field}|${it.code}".toString() }
        throw new ValidationException("Can't save task. Validation errors: ${uncustomizedErrors}")
    }

}

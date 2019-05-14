package io.seqera.watchtower.service

import io.seqera.watchtower.controller.TraceWorkflowRequest
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TraceType
import io.seqera.watchtower.pogo.exceptions.NonExistingTaskException
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import org.springframework.validation.FieldError

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceServiceImpl implements TraceService {

    WorkflowService workflowService

    TaskService taskService

    @Inject
    TraceServiceImpl(WorkflowService workflowService, TaskService taskService) {
        this.workflowService = workflowService
        this.taskService = taskService
    }

    Map<String, Object> createEntityByTrace(Map<String, Object> traceJson) {
        TraceType traceType = identifyTrace(traceJson)

        (traceType == TraceType.WORKFLOW) ? processWorkflowTrace(traceJson) : processTaskTrace(traceJson)
    }

    Map<String, Object> processWorkflowTrace(Map<String, Object> traceJson) {
        Map<String, Object> result = [:]

        result.traceType = TraceType.WORKFLOW
        try {
            Workflow workflow = workflowService.processWorkflowJsonTrace(traceJson as TraceWorkflowRequest)

            String errorMessage = checkWorkflowSaveErrors(workflow)
            if (errorMessage) {
                result.error = errorMessage
            } else {
                result.workflowId = workflow.id
            }
        } catch (NonExistingWorkflowException e) {
            result.error = e.message
        } catch (Exception e) {
            result.error = "Can't process JSON: check format"
        }

        result
    }

    Map<String, Object> processTaskTrace(Map<String, Object> traceJson) {
        Map<String, Object> result = [:]

        result.traceType = TraceType.TASK
        try {
            Task task = taskService.processTaskJsonTrace(traceJson as TraceWorkflowRequest)

            String errorMessage = checkTaskSaveErrors(task)
            if (errorMessage) {
                result.error = errorMessage
            } else {
                result.workflowId = task.workflowId
            }
        } catch (NonExistingWorkflowException e) {
            result.error = e.message
        } catch (NonExistingTaskException e) {
            result.error = e.message
        } catch (Exception e) {
            result.error = "Can't process JSON: check format"
        }

        result
    }

    private String checkWorkflowSaveErrors(Workflow workflow) {
        if (!workflow.hasErrors()) {
            return null
        }

        List<FieldError> fieldErrors = workflow.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            return "Can't save a workflow without ${nullableError.field}"
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            return "Can't save a workflow with the same ${uniqueError.field} of another"
        }
    }

    private String checkTaskSaveErrors(Task task) {
        if (!task.hasErrors()) {
            return null
        }

        List<FieldError> fieldErrors = task.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            return "Can't save a task without ${nullableError.field}"
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            return "Can't save a task with the same ${uniqueError.field} of another"
        }
    }

    private static TraceType identifyTrace(Map<String, Object> traceJson) {
        traceJson.workflow ? TraceType.WORKFLOW : traceJson.task ? TraceType.TASK : null
    }

}

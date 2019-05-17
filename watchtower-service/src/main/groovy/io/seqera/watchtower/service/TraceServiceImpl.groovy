package io.seqera.watchtower.service

import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exceptions.NonExistingTaskException
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse
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


    TraceWorkflowResponse processWorkflowTrace(TraceWorkflowRequest traceJson) {
        TraceWorkflowResponse result = new TraceWorkflowResponse()

        try {
            Workflow workflow = workflowService.processWorkflowJsonTrace(traceJson)

            String errorMessage = checkWorkflowSaveErrors(workflow)
            if (errorMessage) {
                result.message = errorMessage
            } else {
                result.workflowId = workflow.id
            }
        } catch (NonExistingWorkflowException e) {
            result.message = e.message
        } catch (Exception e) {
            result.message = "Can't process JSON: check format"
        }

        result
    }

    TraceTaskResponse processTaskTrace(TraceTaskRequest trace) {
        TraceTaskResponse result = new TraceTaskResponse()

        try {
            Task task = taskService.processTaskJsonTrace(trace)

            String errorMessage = checkTaskSaveErrors(task)
            if (errorMessage) {
                result.message = errorMessage
            } else {
                result.workflowId = task.workflow.id
            }
        } catch (NonExistingWorkflowException e) {
            result.message = e.message
        } catch (NonExistingTaskException e) {
            result.message = e.message
        } catch (Exception e) {
            result.message = "Can't process JSON: check format"
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

        null
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

        null
    }

}

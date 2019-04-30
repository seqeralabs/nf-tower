package io.seqera.watchtower.service

import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TraceType
import io.seqera.watchtower.pogo.exceptions.NonExistingTaskException
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceService {

    @Inject
    WorkflowService workflowService

    @Inject
    TaskService taskService


    Map<String, Object> createEntityByTrace(Map<String, Object> traceJson) {
        TraceType traceType = identifyTrace(traceJson)

        (traceType == TraceType.WORKFLOW) ? processWorkflowTrace(traceJson) : processTaskTrace(traceJson)
    }

    Map<String, Object> processWorkflowTrace(Map<String, Object> traceJson) {
        Map<String, Object> result = [:]

        result.traceType = TraceType.WORKFLOW
        try {
            Workflow workflow = workflowService.processWorkflowJsonTrace(traceJson)

            String errorMessage = checkWorkflowSaveErrors(workflow)
            if (errorMessage) {
                result.error = errorMessage
            } else {
                result.entityId = workflow.id
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
            Task task = taskService.processTaskJsonTrace(traceJson)

            String errorMessage = checkTaskSaveErrors(task)
            if (errorMessage) {
                result.error = errorMessage
            } else {
                result.entityId = task.id
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
        if (workflow.errors.getFieldError('submitTime') || workflow.errors.getFieldError('startTime')) {
            return "Can't complete a non-existing workflow"
        }
        if (workflow.errors.getFieldError('runId') ) {
            return "Can't start an existing workflow"
        }
    }

    private String checkTaskSaveErrors(Task task) {
        if (!task.hasErrors()) {
            return null
        }
        if (task.errors.getFieldError('submitTime')) {
            return "Can't start or complete a non-existing task"
        }
        if (task.errors.getFieldError('task_id') ) {
            return "Can't submit a task which was already submitted"
        }
    }

    private static TraceType identifyTrace(Map<String, Object> traceJson) {
        traceJson.trace ? TraceType.TASK : TraceType.WORKFLOW
    }

}

package watchtower.service.service

import groovy.transform.CompileStatic
import watchtower.service.domain.Workflow
import watchtower.service.pogo.enums.TraceType
import watchtower.service.pogo.exceptions.WorkflowNotExistsException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceService {

    @Inject
    WorkflowService workflowService

    Map<String, Object> createEntityByTrace(Map<String, Object> traceJson) {
        TraceType traceType = identifyTrace(traceJson)

        (traceType == TraceType.WORKFLOW) ? processWorkflowTrace(traceJson) : null
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
        } catch (WorkflowNotExistsException e) {
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

    private static TraceType identifyTrace(Map<String, Object> traceJson) {
        traceJson.trace ? TraceType.TASK : TraceType.WORKFLOW
    }

}

package watchtower.service.service

import groovy.transform.CompileStatic
import watchtower.service.domain.Workflow
import watchtower.service.pogo.enums.TraceType

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@CompileStatic
class TraceService {

    @Inject
    WorkflowService workflowService

    Map<String, Object> createEntityByTrace(Map<String, Object> traceJson) {
        TraceType traceType = identifyTrace(traceJson)

        Map<String, Object> result = [:]
        result.traceType = traceType
        if (traceType == TraceType.WORKFLOW) {
            Workflow workflow = workflowService.processWorkflowJsonTrace(traceJson)
            result.entity = workflow
        }

        result
    }

    private static TraceType identifyTrace(Map<String, Object> traceJson) {
        traceJson.trace ? TraceType.TASK : TraceType.WORKFLOW
    }

}

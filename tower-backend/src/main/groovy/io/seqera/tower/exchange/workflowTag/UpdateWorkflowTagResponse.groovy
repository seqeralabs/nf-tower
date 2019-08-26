package io.seqera.tower.exchange.workflowTag

import groovy.transform.ToString
import io.seqera.tower.domain.WorkflowTag
import io.seqera.tower.exchange.BaseResponse

@ToString
class UpdateWorkflowTagResponse implements BaseResponse {

    WorkflowTag workflowTag
    String message

    static UpdateWorkflowTagResponse ofTag(WorkflowTag workflowTag) {
        new UpdateWorkflowTagResponse(workflowTag: workflowTag)
    }

    static UpdateWorkflowTagResponse ofError(String message) {
        new UpdateWorkflowTagResponse(message: message)
    }
}

package io.seqera.tower.exchange.workflowTag

import groovy.transform.ToString
import io.seqera.tower.domain.WorkflowTag
import io.seqera.tower.exchange.BaseResponse

@ToString
class CreateWorkflowTagResponse implements BaseResponse {

    WorkflowTag workflowTag
    String message

    static CreateWorkflowTagResponse ofTag(WorkflowTag workflowTag) {
        new CreateWorkflowTagResponse(workflowTag: workflowTag)
    }

    static CreateWorkflowTagResponse ofError(String message) {
        new CreateWorkflowTagResponse(message: message)
    }
}

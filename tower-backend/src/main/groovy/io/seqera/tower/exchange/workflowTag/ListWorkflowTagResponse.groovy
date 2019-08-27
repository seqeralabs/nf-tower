package io.seqera.tower.exchange.workflowTag

import io.seqera.tower.domain.WorkflowTag
import io.seqera.tower.exchange.BaseResponse

class ListWorkflowTagResponse implements BaseResponse {

    List<WorkflowTag> workflowTags
    String message

    static ListWorkflowTagResponse ofTags(List<WorkflowTag> workflowTags) {
        new ListWorkflowTagResponse(workflowTags: workflowTags)
    }

    static ListWorkflowTagResponse ofError(String message) {
        new ListWorkflowTagResponse(message: message)
    }

}

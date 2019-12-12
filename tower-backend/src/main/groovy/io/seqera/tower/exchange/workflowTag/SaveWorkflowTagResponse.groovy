package io.seqera.tower.exchange.workflowTag

import io.seqera.tower.domain.WorkflowTag

class SaveWorkflowTagResponse {

    List<WorkflowTag> workflowTopics
    String message

    static SaveWorkflowTagResponse ofTags(List<WorkflowTag> workflowTags) {
        new SaveWorkflowTagResponse(workflowTopics: workflowTags)
    }

    static SaveWorkflowTagResponse ofError(String message) {
        new SaveWorkflowTagResponse(message: message)
    }

}
package io.seqera.tower.exchange.workflowTag

import io.seqera.tower.domain.WorkflowTag

class SaveWorkflowTagRequest {

    String workflowId
    List<WorkflowTag> workflowTopics

}
package io.seqera.tower.service

import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowTag

interface WorkflowTagService {

    List<WorkflowTag> list(Serializable workflowId)

    WorkflowTag get(Serializable id)

    void delete(Serializable id)

    WorkflowTag create(WorkflowTag newWorkflowTag, Workflow associatedWorkflow)

    List<WorkflowTag> save(List<WorkflowTag> newWorkflowTags, Workflow associatedWorkflow)

    WorkflowTag update(WorkflowTag existingTag, WorkflowTag updatedTag)

}

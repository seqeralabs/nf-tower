package io.seqera.tower.service

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowTag

@Transactional
@Service(WorkflowTag)
abstract class WorkflowTagServiceImpl implements WorkflowTagService {

    @Override
    List<WorkflowTag> list(Serializable workflowId) {
        WorkflowTag.where {
            workflow { id == workflowId }
        }.list(sort: 'dateCreated')
    }

    @Override
    WorkflowTag create(WorkflowTag newTag, Workflow associatedWorkflow) {
        newTag.workflow = associatedWorkflow
        newTag.save(failOnError: true)
    }

    @Override
    List<WorkflowTag> save(List<WorkflowTag> newWorkflowTags, Workflow associatedWorkflow) {
        list(associatedWorkflow.id).each { workflowTag ->
            delete(workflowTag.id)
        }
        newWorkflowTags.each { workflowTag ->
            create(workflowTag, associatedWorkflow)
        }
        return newWorkflowTags
    }

    @Override
    WorkflowTag update(WorkflowTag existingTag, WorkflowTag updatedTag) {
        existingTag.text = updatedTag.text
        existingTag.save(failOnError: true)
    }
}

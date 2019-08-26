package io.seqera.tower.service

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowTag

import javax.inject.Singleton

@Transactional
@Service(WorkflowTag)
abstract class WorkflowTagServiceImpl implements WorkflowTagService {

    @Override
    WorkflowTag create(WorkflowTag newTag, Workflow associatedWorkflow) {
        newTag.workflow = associatedWorkflow
        newTag.save(failOnError: true)
    }

    @Override
    WorkflowTag update(WorkflowTag existingTag, WorkflowTag updatedTag) {
        existingTag.label = updatedTag.label
        existingTag.save(failOnError: true)
    }
}

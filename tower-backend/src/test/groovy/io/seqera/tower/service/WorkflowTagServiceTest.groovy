package io.seqera.tower.service

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowTag
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import org.grails.datastore.mapping.validation.ValidationException

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class WorkflowTagServiceTest extends AbstractContainerBaseTest {

    @Inject
    WorkflowTagService workflowTagService


    void "create a new workflow tag"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a new tag'
        WorkflowTag workflowTagToSave = new WorkflowTag(label: 'label')

        when: 'save the tag'
        WorkflowTag savedWorkflowTag = workflowTagService.create(workflowTagToSave, workflow)

        then: 'the tag has been properly saved'
        savedWorkflowTag.id
        savedWorkflowTag.workflowId == workflow.id
        savedWorkflowTag.label == workflowTagToSave.label
    }

    void "try to create a new workflow tag which exceeds the label size"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a new tag with a long label'
        WorkflowTag workflowTagToSave = new WorkflowTag(label: 'a' * 11)

        when: 'save the tag'
        workflowTagService.create(workflowTagToSave, workflow)

        then: 'a validation exception is thrown'
        ValidationException e = thrown(ValidationException)
        e.errors.errorCount == 1
        e.errors.fieldError.field == 'label'
        e.errors.fieldError.code == 'maxSize.exceeded'
    }

    void "try to create a duplicated tag for the same workflow"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a new tag with a label'
        String label = 'label'
        WorkflowTag workflowTagToSave = new WorkflowTag(label: label)

        and: 'another tag with the same label'
        WorkflowTag workflowTagToSaveDuplicated = new WorkflowTag(label: label)

        when: 'save the first tag'
        workflowTagService.create(workflowTagToSave, workflow)

        and: 'try to save the second'
        workflowTagService.create(workflowTagToSaveDuplicated, workflow)

        then: 'a validation exception is thrown'
        ValidationException e = thrown(ValidationException)
        e.errors.errorCount == 1
        e.errors.fieldError.field == 'label'
        e.errors.fieldError.code == 'unique'
    }

    void "update an exsiting workflow tag"() {
        given: 'an existing workflow tag'
        WorkflowTag existingWorkflowTag = new DomainCreator().createWorkflowTag(label: 'oldLabel')

        and: 'a tag to update the existing one'
        WorkflowTag workflowTagTemplate = new WorkflowTag(label: 'newLabel')

        when: 'update the tag'
        WorkflowTag updatedWorkflowTag = workflowTagService.update(existingWorkflowTag, workflowTagTemplate)

        then: 'the tag has been properly updated'
        updatedWorkflowTag.id == existingWorkflowTag.id
        updatedWorkflowTag.label == workflowTagTemplate.label
    }

}

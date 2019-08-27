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
        WorkflowTag workflowTagToSave = new WorkflowTag(text: 'label')

        when: 'save the tag'
        WorkflowTag savedWorkflowTag = WorkflowTag.withNewTransaction {
            workflowTagService.create(workflowTagToSave, workflow)
        }

        then: 'the tag has been properly saved'
        savedWorkflowTag.id
        savedWorkflowTag.workflowId == workflow.id
        savedWorkflowTag.text == workflowTagToSave.text
    }

    void "try to create a new workflow tag which exceeds the text size"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a new tag with a long text'
        WorkflowTag workflowTagToSave = new WorkflowTag(text: 'a' * 11)

        when: 'save the tag'
        WorkflowTag.withNewTransaction {
            workflowTagService.create(workflowTagToSave, workflow)
        }

        then: 'a validation exception is thrown'
        ValidationException e = thrown(ValidationException)
        e.errors.errorCount == 1
        e.errors.fieldError.field == 'text'
        e.errors.fieldError.code == 'maxSize.exceeded'
    }

    void "try to create a duplicated tag for the same workflow"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a new tag with text'
        String text = 'label'
        WorkflowTag workflowTagToSave = new WorkflowTag(text: text)

        and: 'another tag with the same text'
        WorkflowTag workflowTagToSaveDuplicated = new WorkflowTag(text: text)

        when: 'save the first tag'
        WorkflowTag.withNewTransaction {
            workflowTagService.create(workflowTagToSave, workflow)
        }

        and: 'try to save the second'
        WorkflowTag.withNewTransaction {
            workflowTagService.create(workflowTagToSaveDuplicated, workflow)
        }

        then: 'a validation exception is thrown'
        ValidationException e = thrown(ValidationException)
        e.errors.errorCount == 1
        e.errors.fieldError.field == 'text'
        e.errors.fieldError.code == 'unique'
    }

    void "update an exsiting workflow tag"() {
        given: 'an existing workflow tag'
        WorkflowTag existingWorkflowTag = new DomainCreator().createWorkflowTag(text: 'oldLabel')

        and: 'a tag to update the existing one'
        WorkflowTag workflowTagTemplate = new WorkflowTag(text: 'newLabel')

        when: 'update the tag'
        WorkflowTag updatedWorkflowTag = WorkflowTag.withNewTransaction {
            workflowTagService.update(existingWorkflowTag, workflowTagTemplate)
        }

        then: 'the tag has been properly updated'
        updatedWorkflowTag.id == existingWorkflowTag.id
        updatedWorkflowTag.text == workflowTagTemplate.text
    }

    void "get an existing workflow tag by its id"() {
        given: 'an existing workflow tag'
        WorkflowTag existingWorkflowTag = new DomainCreator().createWorkflowTag(text: 'label')

        when: 'get the workflow tag'
        WorkflowTag obtainedWorkflowTag = workflowTagService.get(existingWorkflowTag.id)

        then: 'the tag has been properly obtained'
        obtainedWorkflowTag.id == existingWorkflowTag.id
        obtainedWorkflowTag.text == obtainedWorkflowTag.text
    }

    void "try to get an nonexistent workflow tag by id"() {
        when: 'try to get a workflow tag without providing id'
        WorkflowTag obtainedWorkflowTag = workflowTagService.get(null)

        then: 'the tag is null'
        !obtainedWorkflowTag
    }

    void "delete an existing workflow tag"() {
        given: 'an existing workflow tag'
        WorkflowTag existingWorkflowTag = new DomainCreator().createWorkflowTag(text: 'label')

        expect: 'the workflow tag is persisted in the database'
        WorkflowTag.withNewTransaction {
            WorkflowTag.get(existingWorkflowTag.id)
        }

        when: 'delete the workflow tag'
        WorkflowTag.withNewTransaction {
            workflowTagService.delete(existingWorkflowTag.id)
        }

        then: 'the tag is no longer present in the database'
        WorkflowTag.withNewTransaction {
            !WorkflowTag.get(existingWorkflowTag.id)
        }
    }

}

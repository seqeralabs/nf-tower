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
import java.time.OffsetDateTime

@MicronautTest(application = Application.class)
@Transactional
class WorkflowTagServiceTest extends AbstractContainerBaseTest {

    @Inject
    WorkflowTagService workflowTagService


    void "create a new workflow tag"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a new tag'
        WorkflowTag workflowTagToSave = new WorkflowTag(text: 'label', dateCreated: OffsetDateTime.now().minusSeconds(2))

        when: 'save the tag'
        WorkflowTag savedWorkflowTag = WorkflowTag.withNewTransaction {
            workflowTagService.create(workflowTagToSave, workflow)
        }

        then: 'the tag has been properly saved'
        savedWorkflowTag.id
        savedWorkflowTag.workflowId == workflow.id
        savedWorkflowTag.text == workflowTagToSave.text
        savedWorkflowTag.dateCreated == workflowTagToSave.dateCreated
    }

    void "create a new workflow tag without specifying the creation date explicitly"() {
        given: 'a workflow'
        Workflow workflow = new DomainCreator().createWorkflow()

        and: 'a new tag with text but without creation date'
        WorkflowTag workflowTagToSave = new WorkflowTag(text: 'label')

        when: 'save the tag'
        WorkflowTag savedWorkflowTag = WorkflowTag.withNewTransaction {
            workflowTagService.create(workflowTagToSave, workflow)
        }

        then: 'the tag has been properly saved and has associated offset date'
        savedWorkflowTag.id
        savedWorkflowTag.workflowId == workflow.id
        savedWorkflowTag.text == workflowTagToSave.text
        savedWorkflowTag.dateCreated
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
        updatedWorkflowTag.dateCreated == existingWorkflowTag.dateCreated
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

    void "get a list of workflow tags"() {
        given: 'some workflow tags belonging to a workflow'
        DomainCreator creator = new DomainCreator()
        Workflow aWorkflow = creator.createWorkflow()
        List<WorkflowTag> expectedTags = (1..3).collect { creator.createWorkflowTag(workflow: aWorkflow, dateCreated: OffsetDateTime.now().minusMinutes(it)) }

        and: 'some workflow tags belonging to other workflow'
        Workflow otherWorkflow = creator.createWorkflow()
        (1..3).collect { creator.createWorkflowTag(workflow: otherWorkflow) }

        when: 'get the tags associated with the first workflow'
        List<WorkflowTag> obtainedTags = workflowTagService.list(aWorkflow.id)

        then: 'the tags are as expected and ordered by creation date (older first)'
        obtainedTags.id == expectedTags.sort { it.dateCreated }.id
    }

}

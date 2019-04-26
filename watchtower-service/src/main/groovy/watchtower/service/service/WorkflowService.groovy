package watchtower.service.service

import grails.gorm.services.Service
import groovy.transform.CompileStatic
import watchtower.service.domain.Workflow
import watchtower.service.pogo.exceptions.WorkflowNotExistsException
import watchtower.service.pogo.WorkflowTraceJsonUnmarshaller
import watchtower.service.pogo.enums.WorkflowStatus

import javax.inject.Singleton

@Singleton
@Service(Workflow)
@CompileStatic
abstract class WorkflowService {

    abstract Workflow get(String runId, String runName)

    Workflow processWorkflowJsonTrace(Map workflowJson) {
        WorkflowStatus workflowStatus = WorkflowTraceJsonUnmarshaller.identifyWorflowStatus(workflowJson)

        workflowStatus == WorkflowStatus.STARTED ? createFromJson(workflowJson) : updateFromJson(workflowJson, workflowStatus)
    }

    Workflow createFromJson(Map workflowJson) {
        Workflow newWorkflow = new Workflow()
        WorkflowTraceJsonUnmarshaller.populateWorkflowFields(workflowJson, WorkflowStatus.STARTED, newWorkflow)

        newWorkflow.save()
        newWorkflow
    }

    Workflow updateFromJson(Map workflowJson, WorkflowStatus workflowStatus) {
        Workflow existingWorkflow = get((String) workflowJson.runId, (String) workflowJson.runName as String)
        if (!existingWorkflow) {
            throw new WorkflowNotExistsException("Can't update a non-existing workflow")
        }

        WorkflowTraceJsonUnmarshaller.populateWorkflowFields(workflowJson, workflowStatus, existingWorkflow)

        existingWorkflow.save()
        existingWorkflow
    }

}
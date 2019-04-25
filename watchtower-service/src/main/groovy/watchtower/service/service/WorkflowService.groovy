package watchtower.service.service

import grails.gorm.services.Service
import watchtower.service.domain.Workflow
import watchtower.service.pogo.WorkflowTraceJsonParser
import watchtower.service.pogo.WorkflowStatus

import javax.inject.Singleton

@Singleton
@Service(Workflow)
abstract class WorkflowService {

    abstract Workflow get(String runId, String runName)

    Workflow processWorkflowJsonTrace(Map workflowJson) {
        WorkflowStatus workflowStatus = WorkflowTraceJsonParser.identifyWorflowStatus(workflowJson)

        workflowStatus == WorkflowStatus.STARTED ? createFromJson(workflowJson) : updateFromJson(workflowJson, workflowStatus)
    }

    Workflow createFromJson(Map workflowJson) {
        Workflow newWorkflow = new Workflow()
        WorkflowTraceJsonParser.populateWorkflowFields(workflowJson, WorkflowStatus.STARTED, newWorkflow)

        newWorkflow.save()
        newWorkflow
    }

    Workflow updateFromJson(Map workflowJson, WorkflowStatus workflowStatus) {
        Workflow existingWorkflow = get(workflowJson.runId, workflowJson.runName)
        WorkflowTraceJsonParser.populateWorkflowFields(workflowJson, workflowStatus, existingWorkflow)

        existingWorkflow.save()
        existingWorkflow
    }

}
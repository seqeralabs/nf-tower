package io.seqera.watchtower.service

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.WorkflowTraceJsonUnmarshaller
import io.seqera.watchtower.pogo.enums.WorkflowStatus
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException

import javax.inject.Singleton

@Transactional
@Singleton
@CompileStatic
class WorkflowService {

    @CompileDynamic
    Workflow get(String runId, String runName) {
        Workflow.findByRunIdAndRunName(runId, runName)
    }

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
        Workflow existingWorkflow = get((String) workflowJson.runId, (String) workflowJson.runName)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't update a non-existing workflow")
        }

        WorkflowTraceJsonUnmarshaller.populateWorkflowFields(workflowJson, workflowStatus, existingWorkflow)

        existingWorkflow.save()
        existingWorkflow
    }

}
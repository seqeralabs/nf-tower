package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.controller.TraceWorkflowRequest
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException

import javax.inject.Singleton

@Transactional
@Singleton
class WorkflowServiceImpl implements WorkflowService {

    @CompileDynamic
    Workflow get(Serializable id) {
        Workflow.get(id)
    }

    List<Workflow> list() {
        Workflow.list()
    }


    Workflow processWorkflowJsonTrace(TraceWorkflowRequest traceWorkflowRequest) {
        traceWorkflowRequest.workflow.checkIsStarted() ? createFromJson(traceWorkflowRequest.workflow) : updateFromJson(traceWorkflowRequest.workflow)
    }

    private Workflow createFromJson(Workflow workflow) {
        workflow.submit = workflow.start

        workflow.save()
        workflow
    }

    @CompileDynamic
    private Workflow updateFromJson(Workflow workflow) {
        Workflow existingWorkflow = Workflow.get(workflow.workflowId)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't update a non-existing workflow")
        }

        updateChangeableFields(workflow, existingWorkflow)

        existingWorkflow.save()
        existingWorkflow
    }

    private void updateChangeableFields(Workflow originalWorkflow, Workflow workflowToUpdate) {
        workflowToUpdate.resume = originalWorkflow.resume
        workflowToUpdate.success = originalWorkflow.success
        workflowToUpdate.complete = originalWorkflow.complete
        workflowToUpdate.duration = originalWorkflow.duration

        workflowToUpdate.exitStatus = originalWorkflow.exitStatus
        workflowToUpdate.errorMessage = originalWorkflow.errorMessage
        workflowToUpdate.errorReport = originalWorkflow.errorReport

        workflowToUpdate.stats = originalWorkflow.stats
    }

}
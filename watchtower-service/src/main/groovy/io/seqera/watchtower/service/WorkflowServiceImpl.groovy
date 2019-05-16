package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.controller.TraceWorkflowRequest
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.SummaryEntry
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
        traceWorkflowRequest.workflow.checkIsStarted() ? createFromJson(traceWorkflowRequest.workflow, traceWorkflowRequest.progress) : updateFromJson(traceWorkflowRequest.workflow, traceWorkflowRequest.progress, traceWorkflowRequest.summary)
    }

    private Workflow createFromJson(Workflow workflow, Progress progress) {
        workflow.submit = workflow.start

        workflow.progress = progress
        workflow.save()
        workflow
    }

    @CompileDynamic
    private Workflow updateFromJson(Workflow workflow, Progress progress, List<SummaryEntry> summary) {
        Workflow existingWorkflow = Workflow.get(workflow.workflowId)
        if (!existingWorkflow) {
            throw new NonExistingWorkflowException("Can't update a non-existing workflow")
        }

        associateSummaryEntries(existingWorkflow, summary)
        existingWorkflow.progress = progress
        updateChangeableFields(existingWorkflow, workflow)

        existingWorkflow.save()
        existingWorkflow
    }

    private void updateChangeableFields(Workflow workflowToUpdate, Workflow originalWorkflow) {
        workflowToUpdate.resume = originalWorkflow.resume
        workflowToUpdate.success = originalWorkflow.success
        workflowToUpdate.complete = originalWorkflow.complete
        workflowToUpdate.duration = originalWorkflow.duration

        workflowToUpdate.exitStatus = originalWorkflow.exitStatus
        workflowToUpdate.errorMessage = originalWorkflow.errorMessage
        workflowToUpdate.errorReport = originalWorkflow.errorReport

        workflowToUpdate.stats = originalWorkflow.stats
    }

    private void associateSummaryEntries(Workflow workflow, List<SummaryEntry> summary) {
        summary.each { SummaryEntry summaryEntry ->
            workflow.addToSummaryEntries(summaryEntry)
        }
    }

}
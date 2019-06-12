package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.SummaryEntry
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest

import javax.inject.Inject
import javax.inject.Singleton

@Transactional
@Singleton
class WorkflowServiceImpl implements WorkflowService {

    @CompileDynamic
    Workflow get(Serializable id) {
        Workflow.get(id)
    }

    @CompileDynamic
    List<Workflow> list(User owner) {
        Workflow.findAllByOwner(owner, [sort: 'start', order: 'desc'])
    }

    Workflow processWorkflowJsonTrace(TraceWorkflowRequest traceWorkflowRequest, User owner) {
        traceWorkflowRequest.workflow.checkIsStarted() ? createFromJson(traceWorkflowRequest.workflow, traceWorkflowRequest.progress, owner) : updateFromJson(traceWorkflowRequest.workflow, traceWorkflowRequest.progress, traceWorkflowRequest.summary)
    }

    private Workflow createFromJson(Workflow workflow, Progress progress, User owner) {
        workflow.submit = workflow.start

        workflow.progress = progress
        workflow.owner = owner
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

    void delete(Workflow workflow) {
        workflow.tasks?.each { Task task ->
            task.delete()
        }
        workflow.summaryEntries?.each { SummaryEntry summaryEntry ->
            summaryEntry.delete()
        }

        workflow.delete()
    }

}
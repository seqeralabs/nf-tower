package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.SummaryEntry
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.WorkflowStatus
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.TracesJsonBank

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class WorkflowServiceTest extends AbstractContainerBaseTest {

    @Inject
    WorkflowService workflowService


    void "start a workflow given a started trace"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflow
        Workflow.withNewTransaction {
            workflow = workflowService.processWorkflowJsonTrace(workflowStartedTraceJson)
        }

        then: "the workflow has been correctly saved"
        workflow.id
        workflow.checkIsStarted()
        workflow.submit
        !workflow.complete
        Workflow.count() == 1

        and: "there is progress info"
        workflow.progress.running == 0
        workflow.progress.submitted == 0
        workflow.progress.failed == 0
        workflow.progress.pending == 0
        workflow.progress.succeeded == 0
        workflow.progress.cached == 0
    }

    void "start a workflow given a started trace, then complete the workflow given a succeeded trace"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processWorkflowJsonTrace(workflowStartedTraceJson)
        }

        then: "the workflow has been correctly saved"
        workflowStarted.id
        workflowStarted.checkIsStarted()
        workflowStarted.submit
        !workflowStarted.complete

        when: "given a workflow succeeded trace, unmarshall the succeeded JSON to a workflow"
        TraceWorkflowRequest workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, workflowStarted.id, WorkflowStatus.SUCCEEDED)
        Workflow workflowSucceeded
        Workflow.withNewTransaction {
            workflowSucceeded = workflowService.processWorkflowJsonTrace(workflowSucceededTraceJson)
        }

        then: "the workflow has been completed"
        workflowStarted.id == workflowSucceeded.id
        workflowSucceeded.checkIsSucceeded()
        workflowSucceeded.submit
        workflowSucceeded.complete
        Workflow.count() == 1

        and: "there is summary info"
        workflowSucceeded.summaryEntries.size() == 1
        workflowSucceeded.summaryEntries.first().name == 'sayHello'
        workflowSucceeded.summaryEntries.first().cpu
        workflowSucceeded.summaryEntries.first().time
        workflowSucceeded.summaryEntries.first().reads
        workflowSucceeded.summaryEntries.first().writes
        workflowSucceeded.summaryEntries.first().cpuUsage
        SummaryEntry.count() == 1

        and: "there is progress info"
        workflowSucceeded.progress.running == 0
        workflowSucceeded.progress.submitted == 0
        workflowSucceeded.progress.failed == 0
        workflowSucceeded.progress.pending == 0
        workflowSucceeded.progress.succeeded == 4
        workflowSucceeded.progress.cached == 0
    }

    void "start a workflow given a started trace, then complete the workflow given a failed trace"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processWorkflowJsonTrace(workflowStartedTraceJson)
        }

        then: "the workflow has been correctly saved"
        workflowStarted.id
        workflowStarted.checkIsStarted()
        workflowStarted.submit
        !workflowStarted.complete
        Workflow.count() == 1

        when: "given a workflow failed trace, unmarshall the failed JSON to a workflow"
        TraceWorkflowRequest workflowFailedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, workflowStarted.id, WorkflowStatus.FAILED)
        Workflow workflowFailed
        Workflow.withNewTransaction {
            workflowFailed = workflowService.processWorkflowJsonTrace(workflowFailedTraceJson)
        }

        then: "the workflow has been completed"
        workflowStarted.id == workflowFailed.id
        workflowFailed.checkIsFailed()
        workflowFailed.submit
        workflowFailed.complete
        Workflow.count() == 1

        and: "there is summary info"
        workflowFailed.summaryEntries.size() == 1
        workflowFailed.summaryEntries.first().name == 'sayHello'
        workflowFailed.summaryEntries.first().cpu
        workflowFailed.summaryEntries.first().time
        workflowFailed.summaryEntries.first().reads
        workflowFailed.summaryEntries.first().writes
        workflowFailed.summaryEntries.first().cpuUsage
        SummaryEntry.count() == 1

        and: "there is progress info"
        workflowFailed.progress.running == 0
        workflowFailed.progress.submitted == 0
        workflowFailed.progress.failed == 0
        workflowFailed.progress.pending == 0
        workflowFailed.progress.succeeded == 4
        workflowFailed.progress.cached == 0
    }

    void "start a workflow given a started trace, then try to start the same one"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStarted1TraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted1
        Workflow.withNewTransaction {
            workflowStarted1 = workflowService.processWorkflowJsonTrace(workflowStarted1TraceJson)
        }

        then: "the workflow has been correctly saved"
        workflowStarted1.id
        workflowStarted1.checkIsStarted()
        workflowStarted1.submit
        !workflowStarted1.complete
        Workflow.count() == 1

        when: "given a workflow started trace with the same relatedWorkflowId, unmarshall the started JSON to a second workflow"
        TraceWorkflowRequest workflowStarted2TraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, workflowStarted1.id, WorkflowStatus.STARTED)
        Workflow workflowStarted2
        Workflow.withNewTransaction {
            workflowStarted2 = workflowService.processWorkflowJsonTrace(workflowStarted2TraceJson)
        }

        then: "the second workflow is treated as a new one, and sessionId/runName combination cannot be repeated"
        workflowStarted2.errors.getFieldError('sessionId').code == 'unique'
        Workflow.count() == 1
    }

    void "try to start a workflow given a started trace without sessionId"() {
        given: "a workflow JSON started trace without sessionId"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)
        workflowStartedTraceJson.workflow.sessionId = null

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processWorkflowJsonTrace(workflowStartedTraceJson)
        }

        then: "the workflow has validation errors"
        workflowStarted.hasErrors()
        workflowStarted.errors.getFieldError('sessionId').code == 'nullable'
        Workflow.count() == 0
    }

    void "try to complete a workflow given a succeeded trace for a non existing workflow"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, 123, WorkflowStatus.SUCCEEDED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowSucceeded
        Workflow.withNewTransaction {
            workflowSucceeded = workflowService.processWorkflowJsonTrace(workflowSucceededTraceJson)
        }

        then: "the workflow has been correctly saved"
        thrown(NonExistingWorkflowException)
    }

}

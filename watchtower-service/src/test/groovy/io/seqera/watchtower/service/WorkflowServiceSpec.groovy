package io.seqera.watchtower.service

import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.MagnitudeSummary
import io.seqera.watchtower.domain.ProgressSummary
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.WorkflowStatus
import io.seqera.watchtower.pogo.exceptions.NonExistingWorkflowException
import io.seqera.watchtower.util.AbstractContainerBaseSpec
import io.seqera.watchtower.util.TracesJsonBank

import javax.inject.Inject

@MicronautTest(application = Application.class)
class WorkflowServiceSpec extends AbstractContainerBaseSpec {

    @Inject
    WorkflowService workflowService


    void "start a workflow given a started trace"() {
        given: "a workflow JSON started trace"
        Map workflowTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflow
        Workflow.withNewTransaction {
            workflow = workflowService.processWorkflowJsonTrace(workflowTraceJson)
        }

        then: "the workflow has been correctly saved"
        workflow.id
        workflow.currentStatus == WorkflowStatus.STARTED
        workflow.submitTime
        !workflow.completeTime
        Workflow.count() == 1

        and: "the workflow has an associated progress object"
        workflow.progressSummary.id
        ProgressSummary.count() == 1
    }

    void "start a workflow given a started trace, then complete the workflow given a succeeded trace"() {
        given: "a workflow JSON started trace"
        Map workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processWorkflowJsonTrace(workflowStartedTraceJson)
        }

        then: "the workflow has been correctly saved"
        workflowStarted.id
        workflowStarted.currentStatus == WorkflowStatus.STARTED
        workflowStarted.submitTime
        !workflowStarted.completeTime

        when: "given a workflow succeeded trace, unmarshall the succeeded JSON to a workflow"
        Map workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, workflowStarted.id, WorkflowStatus.SUCCEEDED)
        Workflow workflowSucceeded
        Workflow.withNewTransaction {
            workflowSucceeded = workflowService.processWorkflowJsonTrace(workflowSucceededTraceJson)
        }

        then: "the workflow has been completed"
        workflowStarted.id == workflowSucceeded.id
        workflowSucceeded.currentStatus == WorkflowStatus.SUCCEEDED
        workflowSucceeded.submitTime
        workflowSucceeded.completeTime
        Workflow.count() == 1

        and: "there is summary info"
        workflowSucceeded.magnitudeSummaries.size() == 5
        workflowSucceeded.magnitudeSummaries.taskLabel.every { it == 'sayHello' }
        workflowSucceeded.magnitudeSummaries.name as Set == ['cpu', 'time', 'reads', 'writes', 'cpuUsage'] as Set
        MagnitudeSummary.count() == 5
    }

    void "start a workflow given a started trace, then complete the workflow given a failed trace"() {
        given: "a workflow JSON started trace"
        Map workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processWorkflowJsonTrace(workflowStartedTraceJson)
        }

        then: "the workflow has been correctly saved"
        workflowStarted.id
        workflowStarted.currentStatus == WorkflowStatus.STARTED
        workflowStarted.submitTime
        !workflowStarted.completeTime
        Workflow.count() == 1

        when: "given a workflow failed trace, unmarshall the failed JSON to a workflow"
        Map workflowFailedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, workflowStarted.id, WorkflowStatus.FAILED)
        Workflow workflowFailed
        Workflow.withNewTransaction {
            workflowFailed = workflowService.processWorkflowJsonTrace(workflowFailedTraceJson)
        }

        then: "the workflow has been completed"
        workflowStarted.id == workflowFailed.id
        workflowFailed.currentStatus == WorkflowStatus.FAILED
        workflowFailed.submitTime
        workflowFailed.completeTime
        Workflow.count() == 1

        and: "there is summary info"
        workflowFailed.magnitudeSummaries.size() == 5
        workflowFailed.magnitudeSummaries.taskLabel.every { it == 'sayHello' }
        workflowFailed.magnitudeSummaries.name as Set == ['cpu', 'time', 'reads', 'writes', 'cpuUsage'] as Set
        MagnitudeSummary.count() == 5
    }

    void "start a workflow given a started trace, then try to start the same one"() {
        given: "a workflow JSON started trace"
        Map workflowStarted1TraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted1
        Workflow.withNewTransaction {
            workflowStarted1 = workflowService.processWorkflowJsonTrace(workflowStarted1TraceJson)
        }

        then: "the workflow has been correctly saved"
        workflowStarted1.id
        workflowStarted1.currentStatus == WorkflowStatus.STARTED
        workflowStarted1.submitTime
        !workflowStarted1.completeTime
        Workflow.count() == 1

        when: "given a workflow started trace with the same workflowId, unmarshall the started JSON to a second workflow"
        Map workflowStarted2TraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, workflowStarted1.id, WorkflowStatus.STARTED)
        Workflow workflowStarted2
        Workflow.withNewTransaction {
            workflowStarted2 = workflowService.processWorkflowJsonTrace(workflowStarted2TraceJson)
        }

        then: "the workflow status is treated as a pause, so the data is updated"
        workflowStarted1.id == workflowStarted2.id
        workflowStarted1.currentStatus == WorkflowStatus.PAUSED
        workflowStarted1.submitTime
        !workflowStarted1.completeTime
        Workflow.count() == 1
    }

    void "try to start a workflow given a started trace without sessionId"() {
        given: "a workflow JSON started trace without sessionId"
        Map workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, null, WorkflowStatus.STARTED)
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
        Map workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace(1, 123, WorkflowStatus.SUCCEEDED)

        when: "unmarshall the JSON to a workflow"
        Workflow workflowSucceeded
        Workflow.withNewTransaction {
            workflowSucceeded = workflowService.processWorkflowJsonTrace(workflowSucceededTraceJson)
        }

        then: "the workflow has been correctly saved"
        thrown(NonExistingWorkflowException)
    }

}

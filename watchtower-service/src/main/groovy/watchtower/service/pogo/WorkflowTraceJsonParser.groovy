package watchtower.service.pogo

import com.fasterxml.jackson.databind.ObjectMapper
import watchtower.service.domain.Workflow

import java.time.Instant

class WorkflowTraceJsonParser {

    static WorkflowStatus identifyWorflowStatus(Map workflowJson) {
        WorkflowStatus workflowStatus = null

        if (workflowJson.event == 'started') {
            workflowStatus = workflowJson.workflow.resume ? WorkflowStatus.RESUMED : WorkflowStatus.STARTED
        } else if (workflowJson.event == 'completed') {
            workflowStatus = workflowJson.workflow.success ? WorkflowStatus.SUCCEEDED : WorkflowStatus.FAILED
        } else if (workflowJson.event == 'paused') {
            workflowStatus = WorkflowStatus.PAUSED
        }

        workflowStatus
    }

    static Workflow populateWorkflowFields(Map workflowJson, WorkflowStatus workflowStatus, Workflow workflow) {
        workflow.currentStatus = workflowStatus
        workflowJson.each { String k, def v ->
            if (k == 'utcTime') {
                populateStatusTimestamp(v, workflowStatus, workflow)
            } else if (k == 'metadata') {
                populateMetaData(v, workflow)
            } else {
                workflow[k] = v
            }
        }

        workflow
    }

    private static void populateMetaData(Map workflowMetaData, Workflow workflow) {
        workflowMetaData.each { String k, def v ->
            if (k == 'parameters') {
                workflow[k] = new ObjectMapper().writeValueAsString(v)
            } else if (k == 'workflow') {
                populateMainData(v, workflow)
            } else {
                workflow[k] = v
            }
        }
    }

    private static void populateMainData(Map workflowMainData, Workflow workflow) {
        workflowMainData.each { String k, def v ->
            if (k == 'manifest') {
                workflowMainData[k].each { String manifestPropertyKey, def manifestPropertyValue ->
                    workflow."manifest${manifestPropertyKey}" = manifestPropertyValue
                }
            } else if (k == 'nextflow') {
                workflowMainData[k].each { String nextflowPropertyKey, def nextflowPropertyValue ->
                    workflow."nextflow${nextflowPropertyKey}" = nextflowPropertyValue
                }
            } else if (k == 'stats') {
                workflowMainData[k].each { String statsPropertyKey, def statsPropertyValue ->
                    workflow[statsPropertyKey] = statsPropertyValue
                }
            } else if (k == 'configFiles') {
                workflow[k] = new ObjectMapper().writeValueAsString(v)
            } else {
                workflow[k] = v
            }
        }
    }

    private static void populateStatusTimestamp(String timestamp, WorkflowStatus workflowStatus, Workflow workflow) {
        Instant instant = Instant.parse(timestamp)

        if (workflowStatus == WorkflowStatus.STARTED) {
            workflow.submitTime = instant
            workflow.startTime = instant
        } else if (workflowStatus == WorkflowStatus.RESUMED) {
            workflow.startTime = instant
        } else if (workflowStatus == WorkflowStatus.SUCCEEDED || workflowStatus == WorkflowStatus.FAILED) {
            workflow.completeTime = instant
        }
    }

}

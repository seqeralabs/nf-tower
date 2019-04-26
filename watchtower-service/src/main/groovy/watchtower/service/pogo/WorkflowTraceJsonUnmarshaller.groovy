package watchtower.service.pogo

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import watchtower.service.domain.Workflow
import watchtower.service.pogo.enums.WorkflowStatus

import java.time.Instant

@CompileStatic
class WorkflowTraceJsonUnmarshaller {

    static WorkflowStatus identifyWorflowStatus(Map workflowJson) {
        WorkflowStatus workflowStatus = null

        if (workflowJson.event == 'started') {
            workflowStatus = workflowJson.metadata['workflow']['resume'] ? WorkflowStatus.RESUMED : WorkflowStatus.STARTED
        } else if (workflowJson.event == 'completed') {
            workflowStatus = workflowJson.metadata['workflow']['success'] ? WorkflowStatus.SUCCEEDED : WorkflowStatus.FAILED
        } else if (workflowJson.event == 'paused') {
            workflowStatus = WorkflowStatus.PAUSED
        }

        workflowStatus
    }

    @CompileDynamic
    static Workflow populateWorkflowFields(Map<String, Object> workflowJson, WorkflowStatus workflowStatus, Workflow workflow) {
        workflow.currentStatus = workflowStatus
        workflowJson.each { String k, def v ->
            if (k == 'utcTime') {
                populateStatusTimestamp((String) v, workflowStatus, workflow)
            } else if (k == 'metadata') {
                populateMetaData((Map<String, Object>) v, workflow)
            } else {
                workflow[k] = v
            }
        }

        workflow
    }

    @CompileDynamic
    private static void populateMetaData(Map<String, Object> workflowMetaData, Workflow workflow) {
        workflowMetaData.each { String k, def v ->
            if (k == 'parameters') {
                workflow.parameters = new ObjectMapper().writeValueAsString(v)
            } else if (k == 'workflow') {
                populateMainData((Map<String, Object>) v, workflow)
            } else {
                workflow[k] = v
            }
        }
    }

    @CompileDynamic
    private static void populateMainData(Map<String, Object> workflowMainData, Workflow workflow) {
        workflowMainData.each { String k, def v ->
            if (k == 'manifest') {
                workflowMainData[k].each { String manifestPropertyKey, def manifestPropertyValue ->
                    workflow["manifest${manifestPropertyKey.capitalize()}"] = manifestPropertyValue
                }
            } else if (k == 'nextflow') {
                workflowMainData[k].each { String nextflowPropertyKey, def nextflowPropertyValue ->
                    workflow["nextflow${nextflowPropertyKey.capitalize()}"] = nextflowPropertyValue
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

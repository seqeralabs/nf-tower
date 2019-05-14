package io.seqera.watchtower.pogo.unmarshaller

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.MagnitudeSummary
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.WorkflowStatus

import java.time.Instant

class WorkflowTraceJsonUnmarshaller {

    static WorkflowStatus identifyWorkflowStatus(Map workflowJson) {
        if (workflowJson.workflow['workflowId']) {
            workflowJson.workflow['complete'] ? (workflowJson.workflow['success'] ? WorkflowStatus.SUCCEEDED : WorkflowStatus.FAILED) : (workflowJson.workflow['resume'] ? WorkflowStatus.RESUMED : WorkflowStatus.PAUSED)
        } else {
            WorkflowStatus.STARTED
        }
    }

    static void populateWorkflowFields(Map<String, Object> workflowJson, WorkflowStatus workflowStatus, Workflow workflow) {
//        workflow.status = workflowStatus
        workflowJson.each { String k, def v ->
            if (k == 'utcTime') {
                populateStatusTimestamp((String) v, workflowStatus, workflow)
            } else if (k == 'workflow') {
                populateMainData((Map<String, Object>) v, workflow)
            } else if (k == 'progress') {
                populateProgressData((Map<String, Object>) v, workflow)
            } else if (k == 'summary') {
                populateSummaryData((Map<String, Object>) v, workflow)
            }
        }
    }

    @CompileDynamic
    static void populateProgressData(Map<String, Object> progressData, Workflow workflow) {
        progressData.each { String k, def v ->
            if (!isIgnoredField(k, workflow)) {
                workflow[k] = v
            }
        }
    }

    @CompileDynamic
    private static void populateMainData(Map<String, Object> workflowMainData, Workflow workflow) {
        workflowMainData.each { String k, def v ->
            if (k == 'params') {
                workflow.params = new ObjectMapper().writeValueAsString(v)
            } else if (k == 'manifest') {
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
            } else if (!isIgnoredField(k, workflow)) {
                workflow[k] = v
            }
        }
    }

    private static void populateSummaryData(Map<String, Object> summaryData, Workflow workflow) {
        Collection<MagnitudeSummary> magnitudeSummaries = MagnitudeSummaryJsonUnmarshaller.extractAllMagnitudeSummaries(summaryData)

        magnitudeSummaries.each { MagnitudeSummary magnitudeSummary ->
            magnitudeSummary.workflow = workflow
            workflow.addToMagnitudeSummaries(magnitudeSummary)
        }
    }

    private static boolean isIgnoredField(String key, Workflow workflow) {
        !workflow.hasProperty(key) || (key == 'workflowId') || (key == 'status')
    }

    private static void populateStatusTimestamp(String timestamp, WorkflowStatus workflowStatus, Workflow workflow) {
        Instant instant = Instant.parse(timestamp)

        if (workflowStatus == WorkflowStatus.STARTED) {
//            workflow.submitTime = instant
//            workflow.startTime = instant
        } else if (workflowStatus == WorkflowStatus.RESUMED) {
//            workflow.startTime = instant
        } else if (workflowStatus == WorkflowStatus.SUCCEEDED || workflowStatus == WorkflowStatus.FAILED) {
//            workflow.completeTime = instant
        }
    }

}

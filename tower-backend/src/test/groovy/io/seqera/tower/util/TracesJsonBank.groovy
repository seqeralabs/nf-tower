/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.util

import java.util.regex.Matcher

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient
import io.seqera.tower.domain.User
import io.seqera.tower.exchange.trace.TraceBeginRequest
import io.seqera.tower.exchange.trace.TraceCompleteRequest
import io.seqera.tower.exchange.trace.TraceRecordRequest
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceTaskResponse
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.exchange.trace.TraceWorkflowResponse
import io.seqera.tower.service.auth.AuthenticationByApiToken

enum WorkflowTraceSnapshotStatus {
    STARTED, SUCCEEDED, FAILED, MALFORMED
}

enum TaskTraceSnapshotStatus {
    SUBMITTED, STARTED, RUNNING, COMPLETED, SUCCEEDED, FAILED, MULTITASK, MALFORMED
}

@Slf4j
class TracesJsonBank {


    private final static RESOURCES_DIR_PATH = 'src/test/resources'

    private static File getWorkflowDir(String workflowLabel) {
        new File(RESOURCES_DIR_PATH, "workflow_${workflowLabel}")
    }

    static TraceBeginRequest extractTraceBeginRequest(String workflowLabel, String workflowId) {
        File workflowDir = getWorkflowDir(workflowLabel)

        String fileNamePart = "workflow_started.json"
        File jsonFile = workflowDir.listFiles().find { it.name.endsWith(fileNamePart) }
        log.debug "Test json file: $jsonFile"
        def workflowTrace = DomainHelper.mapper.readValue(jsonFile, TraceBeginRequest.class)
        if( workflowId )
            workflowTrace.workflow.id = workflowId

        return workflowTrace
    }

    static TraceCompleteRequest extractTraceCompleteRequest(String workflowLabel, String workflowId) {
        File workflowDir = getWorkflowDir(workflowLabel)

        String fileNamePart = "workflow_succeeded.json"
        File jsonFile = workflowDir.listFiles().find { it.name.endsWith(fileNamePart) }
        log.debug "Test json file: $jsonFile"
        def workflowTrace = DomainHelper.mapper.readValue(jsonFile, TraceCompleteRequest.class)
        if( workflowId )
            workflowTrace.workflow.id = workflowId

        return workflowTrace
    }

    static TraceRecordRequest extractTraceRecord(String workflowLabel, Long taskId, String workflowId, TaskTraceSnapshotStatus taskStatus) {
        File workflowDir = getWorkflowDir(workflowLabel)

        String fileNamePart = "task_${taskId}_${taskStatus.name().toLowerCase()}.json"
        File jsonFile = workflowDir.listFiles().sort { it.name }.find { it.name.endsWith(fileNamePart) }
        println "JsonFile=$jsonFile"

        TraceRecordRequest taskTrace = DomainHelper.mapper.readValue(jsonFile, TraceRecordRequest)
        taskTrace.workflowId = workflowId

        taskTrace
    }

    static TraceWorkflowRequest extractWorkflowJsonTrace(String workflowLabel, String workflowId, WorkflowTraceSnapshotStatus workflowStatus) {
        File workflowDir = getWorkflowDir(workflowLabel)

        String fileNamePart = "workflow_${workflowStatus.name().toLowerCase()}.json"
        File jsonFile = workflowDir.listFiles().find { it.name.endsWith(fileNamePart) }
        log.debug "Test json file: $jsonFile"
        TraceWorkflowRequest workflowTrace = DomainHelper.mapper.readValue(jsonFile, TraceWorkflowRequest.class)
        if( workflowId )
            workflowTrace.workflow.id = workflowId

        workflowTrace
    }

    static TraceTaskRequest extractTaskJsonTrace(String workflowLabel, Long taskId, String workflowId, TaskTraceSnapshotStatus taskStatus) {
        File workflowDir = getWorkflowDir(workflowLabel)

        String fileNamePart = "task_${taskId}_${taskStatus.name().toLowerCase()}.json"
        File jsonFile = workflowDir.listFiles().sort { it.name }.find { it.name.endsWith(fileNamePart) }
        println "JsonFile=$jsonFile"

        TraceTaskRequest taskTrace = DomainHelper.mapper.readValue(jsonFile, TraceTaskRequest.class)
        taskTrace.workflowId = workflowId

        taskTrace
    }

    static List<Map> getTasksFeatures(String workflowLabel) {
        File workflowDir = new File("${RESOURCES_DIR_PATH}/workflow_${workflowLabel}/")

        List<File> jsonFiles = workflowDir.listFiles().toList()

        jsonFiles.name.findResults { String filename ->
            Matcher matcher = (filename =~ /(\d+)_task_(\d+)_(\w+).json/)
            if (matcher) {
                [
                    order: matcher.group(1).toLong(),
                    taskId: matcher.group(2).toLong(),
                    status: matcher.group(3),
                ]
            } else {
                null
            }
        }
    }

}

class NextflowSimulator {

    private static final String WORKFLOW_TRACE_ENDPOINT = '/trace/workflow'
    private static final String TASK_TRACE_ENDPOINT = '/trace/task'

    User user
    String workflowLabel
    BlockingHttpClient client
    Long sleepBetweenRequests
    String workflowId

    private List<TraceTaskRequest> tasksRequestSequence


    void simulate(Integer nRequests = null) {
        if (!workflowId) {
            TraceWorkflowRequest workflowStarted = TracesJsonBank.extractWorkflowJsonTrace(workflowLabel, null, WorkflowTraceSnapshotStatus.STARTED)
            workflowStarted.workflow.id = "id-${new Random().nextInt(1000)}".toString()
            HttpResponse<TraceWorkflowResponse> workflowResponse = client.exchange(buildRequest(WORKFLOW_TRACE_ENDPOINT, workflowStarted), TraceWorkflowResponse.class)
            workflowId = workflowResponse.body().workflowId

            if ((nRequests != null) && (--nRequests == 0)) {
                return
            }
        }

        tasksRequestSequence = tasksRequestSequence ?: computeTaskTraceSequence()
        Iterator<TraceTaskRequest> i = tasksRequestSequence.iterator()
        while (i.hasNext()) {
            TraceTaskRequest taskRequest = i.next()
            i.remove()

            sleepIfSet()
            client.exchange(buildRequest(TASK_TRACE_ENDPOINT, taskRequest), TraceTaskResponse.class)

            if ((nRequests != null) && (--nRequests == 0)) {
                return
            }
        }

        TraceWorkflowRequest workflowCompleted = TracesJsonBank.extractWorkflowJsonTrace(workflowLabel, workflowId, WorkflowTraceSnapshotStatus.SUCCEEDED)
        client.exchange(buildRequest(WORKFLOW_TRACE_ENDPOINT, workflowCompleted), TraceWorkflowResponse.class)
    }

    private buildRequest(String endpoint, def body) {
        HttpRequest.POST(endpoint, body).basicAuth(AuthenticationByApiToken.ID, user.accessTokens.first().token)
    }

    private void sleepIfSet() {
        if (sleepBetweenRequests) {
            sleep(sleepBetweenRequests)
        }
    }

    private List<TraceTaskRequest> computeTaskTraceSequence() {
        List<Map> tasksFeatures = TracesJsonBank.getTasksFeatures(workflowLabel).sort { it.order }

        tasksFeatures.collect { Map taskFeatures ->
            TracesJsonBank.extractTaskJsonTrace(workflowLabel, taskFeatures.taskId, workflowId, TaskTraceSnapshotStatus."${taskFeatures.status.toUpperCase()}")
        }
    }

}

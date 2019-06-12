package io.seqera.watchtower.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse

import java.util.regex.Matcher

enum WorkflowTraceSnapshotStatus {
    STARTED, SUCCEEDED, FAILED, MALFORMED
}

enum TaskTraceSnapshotStatus {
    SUBMITTED, STARTED, RUNNING, COMPLETED, SUCCEEDED, FAILED, MALFORMED
}

class TracesJsonBank {

    private final static RESOURCES_DIR_PATH = 'src/test/resources'

    private static File getWorkflowDir(String workflowLabel) {
        new File(RESOURCES_DIR_PATH, "workflow_${workflowLabel}")
    }

    static TraceWorkflowRequest extractWorkflowJsonTrace(String workflowLabel, Long workflowId, WorkflowTraceSnapshotStatus workflowStatus) {
        File workflowDir = getWorkflowDir(workflowLabel)

        String fileNamePart = "workflow_${workflowStatus.name().toLowerCase()}.json"
        File jsonFile = workflowDir.listFiles().find { it.name.endsWith(fileNamePart) }

        TraceWorkflowRequest workflowTrace = new ObjectMapper().readValue(jsonFile, TraceWorkflowRequest.class)
        workflowTrace.workflow.workflowId = workflowId

        workflowTrace
    }

    static TraceTaskRequest extractTaskJsonTrace(String workflowLabel, Integer taskId, Long workflowId, TaskTraceSnapshotStatus taskStatus) {
        File workflowDir = getWorkflowDir(workflowLabel)

        String fileNamePart = "task_${taskId}_${taskStatus.name().toLowerCase()}.json"
        File jsonFile = workflowDir.listFiles().find { it.name.endsWith(fileNamePart) }
        println "JsonFile=$jsonFile"

        TraceTaskRequest taskTrace = new ObjectMapper().readValue(jsonFile, TraceTaskRequest.class)
        taskTrace.task.relatedWorkflowId = workflowId

        taskTrace
    }

    static List<Map> getTasksFeatures(String workflowLabel) {
        File workflowDir = new File("${RESOURCES_DIR_PATH}/workflow_${workflowLabel}/")

        List<File> jsonFiles = workflowDir.listFiles().toList()

        jsonFiles.name.findResults { String filename ->
            Matcher matcher = (filename =~ /(\d+)_task_(\d+)_(\w+).json/)
            if (matcher) {
                [
                    order: matcher.group(1).toInteger(),
                    taskId: matcher.group(2).toInteger(),
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
    Long workflowId

    private List<TraceTaskRequest> tasksRequestSequence


    void simulate(Integer nRequests = null) {
        if (!workflowId) {
            TraceWorkflowRequest workflowStarted = TracesJsonBank.extractWorkflowJsonTrace(workflowLabel, null, WorkflowTraceSnapshotStatus.STARTED)
            HttpResponse<TraceWorkflowResponse> workflowResponse = client.exchange(buildRequest(WORKFLOW_TRACE_ENDPOINT, workflowStarted), TraceWorkflowResponse.class)
            workflowId = workflowResponse.body().workflowId.toLong()

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
        HttpRequest.POST(endpoint, body).basicAuth(user.userName, user.accessTokens.first().token)
    }

    private void sleepIfSet() {
        if (sleepBetweenRequests) {
            sleep(sleepBetweenRequests)
        }
    }

    private List<TraceTaskRequest> computeTaskTraceSequence() {
        List<Map> tasksFeatures = TracesJsonBank.getTasksFeatures(workflowLabel).sort { it.order }

        tasksFeatures.collect {
            TracesJsonBank.extractTaskJsonTrace(workflowLabel, it.taskId, workflowId, TaskTraceSnapshotStatus."${it.status.toUpperCase()}")
        }
    }

}

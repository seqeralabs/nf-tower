package io.seqera.watchtower.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient
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

    private static File getWorkflowDir(Integer workflowOrder) {
        new File(RESOURCES_DIR_PATH, "workflow_${workflowOrder}")
    }

    static TraceWorkflowRequest extractWorkflowJsonTrace(Integer workflowOrder, Long workflowId, WorkflowTraceSnapshotStatus workflowStatus) {
        File workflowDir = getWorkflowDir(workflowOrder)

        String fileNamePart = "workflow_${workflowStatus.name().toLowerCase()}.json"
        File jsonFile = workflowDir.listFiles().find { it.name.endsWith(fileNamePart) }

        TraceWorkflowRequest workflowTrace = new ObjectMapper().readValue(jsonFile, TraceWorkflowRequest.class)
        workflowTrace.workflow.workflowId = workflowId

        workflowTrace
    }

    static TraceTaskRequest extractTaskJsonTrace(Integer workflowOrder, Integer taskOrder, Long workflowId, TaskTraceSnapshotStatus taskStatus) {
        File workflowDir = getWorkflowDir(workflowOrder)

        String fileNamePart = "task_${taskOrder}_${taskStatus.name().toLowerCase()}.json"
        File jsonFile = workflowDir.listFiles().find { it.name.endsWith(fileNamePart) }

        TraceTaskRequest taskTrace = new ObjectMapper().readValue(jsonFile, TraceTaskRequest.class)
        taskTrace.task.relatedWorkflowId = workflowId

        taskTrace
    }

    static List<Integer> getTasksIds(Integer workflowOrder) {
        File workflowDir = new File("${RESOURCES_DIR_PATH}/workflow_${workflowOrder}/")

        List<File> jsonFiles = workflowDir.listFiles().toList()

        jsonFiles.name.findResults { String filename ->
            if (!filename.startsWith('task')) {
                return null
            }

            Matcher matcher = (filename =~ /task_(\d+)_\w+.json/)
            if (matcher) {
                matcher.group(1).toInteger()
            }
        }.unique().sort()
    }

}

class NextflowSimulator {

    private static final String WORKFLOW_TRACE_ENDPOINT = '/trace/workflow'
    private static final String TASK_TRACE_ENDPOINT = '/trace/task'

    Integer workflowOrder
    BlockingHttpClient client
    Long sleepBetweenRequests
    Long workflowId

    private List<TraceTaskRequest> tasksRequestSequence


    void simulate(Integer nRequests = null) {
        if (!workflowId) {
            TraceWorkflowRequest workflowStarted = TracesJsonBank.extractWorkflowJsonTrace(workflowOrder, null, WorkflowTraceSnapshotStatus.STARTED)
            HttpResponse<TraceWorkflowResponse> workflowResponse = client.exchange(HttpRequest.POST(WORKFLOW_TRACE_ENDPOINT, workflowStarted), TraceWorkflowResponse.class)
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
            client.exchange(HttpRequest.POST(TASK_TRACE_ENDPOINT, taskRequest), TraceTaskResponse.class)

            if ((nRequests != null) && (--nRequests == 0)) {
                return
            }
        }

        TraceWorkflowRequest workflowCompleted = TracesJsonBank.extractWorkflowJsonTrace(workflowOrder, workflowId, WorkflowTraceSnapshotStatus.SUCCEEDED)
        client.exchange(HttpRequest.POST(WORKFLOW_TRACE_ENDPOINT, workflowCompleted), TraceWorkflowResponse.class)
    }

    private void sleepIfSet() {
        if (sleepBetweenRequests) {
            sleep(sleepBetweenRequests)
        }
    }

    private List<TraceTaskRequest> computeTaskTraceSequence() {
        List<Integer> tasksOrders = TracesJsonBank.getTasksIds(workflowOrder)

        List<TraceTaskRequest> taskSubmittedTraces = tasksOrders.collect { Integer taskOrder -> TracesJsonBank.extractTaskJsonTrace(workflowOrder, taskOrder, workflowId, TaskTraceSnapshotStatus.SUBMITTED) }
        List<TraceTaskRequest> taskRunningTraces = tasksOrders.collect { Integer taskOrder -> TracesJsonBank.extractTaskJsonTrace(workflowOrder, taskOrder, workflowId, TaskTraceSnapshotStatus.RUNNING) }
        List<TraceTaskRequest> taskCompletedTraces = tasksOrders.collect { Integer taskOrder -> TracesJsonBank.extractTaskJsonTrace(workflowOrder, taskOrder, workflowId, TaskTraceSnapshotStatus.COMPLETED) }

        List<TraceTaskRequest> tracesSequence = []

        Random random = new Random()
        int nTakenElementsSubmitted = 0, nTakenElementsRunning = 0, nTakenElementsCompleted = 0
        while (!(taskSubmittedTraces.isEmpty() && taskRunningTraces.isEmpty() && taskCompletedTraces.isEmpty())) {
            int nElementsSubmittedToTake = random.nextInt(taskSubmittedTraces.size() + 1)
            nElementsSubmittedToTake.times {
                takeFirstElementFromList(taskSubmittedTraces, tracesSequence)
                nTakenElementsSubmitted++
            }

            int nElementsRunningToTake = random.nextInt(nTakenElementsSubmitted - nTakenElementsRunning + 1)
            nElementsRunningToTake.times {
                takeFirstElementFromList(taskRunningTraces, tracesSequence)
                nTakenElementsRunning++
            }

            int nElementsCompletedToTake = random.nextInt(nTakenElementsRunning - nTakenElementsCompleted + 1)
            nElementsCompletedToTake.times {
                takeFirstElementFromList(taskCompletedTraces, tracesSequence)
                nTakenElementsCompleted++
            }
        }

        tracesSequence
    }

    private static void takeFirstElementFromList(List originalList, List accumulationList) {
        accumulationList << originalList.first()
        originalList.remove(0)
    }

}

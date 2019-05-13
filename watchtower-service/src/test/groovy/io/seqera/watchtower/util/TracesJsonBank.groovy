package io.seqera.watchtower.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.BlockingHttpClient
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.enums.WorkflowStatus
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse

import java.util.regex.Matcher

class TracesJsonBank {

    private final static RESOURCES_DIR_PATH = 'src/test/resources'

    static TraceWorkflowRequest extractWorkflowJsonTrace(Integer workflowOrder, Long workflowId, WorkflowStatus workflowStatus) {
        String fileRelativePath = "workflow_${workflowOrder}/workflow_${workflowStatus.name().toLowerCase()}.json"
        File jsonFile = new File("${RESOURCES_DIR_PATH}/${fileRelativePath}")

        TraceWorkflowRequest workflowTrace = new ObjectMapper().readValue(jsonFile, TraceWorkflowRequest.class)
        workflowTrace.workflow.workflowId = workflowId

        workflowTrace
    }

    static TraceTaskRequest extractTaskJsonTrace(Integer workflowOrder, Integer taskOrder, Long workflowId, TaskStatus taskStatus) {
        String fileRelativePath = "workflow_${workflowOrder}/task_${taskOrder}_${taskStatus.name().toLowerCase()}.json"
        File jsonFile = new File("${RESOURCES_DIR_PATH}/${fileRelativePath}")

        TraceTaskRequest taskTrace = new ObjectMapper().readValue(jsonFile, TraceTaskRequest.class)
        taskTrace.task.relatedWorkflowId = workflowId

        taskTrace
    }

    static List<Integer> getUniqueTasksOrders(Integer workflowOrder) {
        File workflowDir = new File("${RESOURCES_DIR_PATH}/workflow_${workflowOrder}/")

        List<File> jsonFiles = workflowDir.listFiles().toList()

        jsonFiles.name.findResults { String filename ->
            if (!filename.startsWith('task')) {
                return null
            }

            Matcher matcher = (filename =~ /\d+/)
            if (matcher) {
                matcher.group().toInteger()
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


    void simulate() {
        TraceWorkflowRequest workflowStarted = TracesJsonBank.extractWorkflowJsonTrace(workflowOrder, null, WorkflowStatus.STARTED)
        HttpResponse<TraceWorkflowResponse> workflowResponse = client.exchange(HttpRequest.POST(WORKFLOW_TRACE_ENDPOINT, workflowStarted), TraceWorkflowResponse.class)
        Long workflowId = workflowResponse.body().workflowId.toLong()

        List<TraceTaskRequest> tasksRequestSequence = computeTaskTraceSequence(workflowId)

        tasksRequestSequence.each { TraceTaskRequest taskRequest ->
            sleepIfSet()
            client.exchange(HttpRequest.POST(TASK_TRACE_ENDPOINT, taskRequest), TraceTaskResponse.class)
        }

        TraceWorkflowRequest workflowCompleted = TracesJsonBank.extractWorkflowJsonTrace(workflowOrder, workflowId, WorkflowStatus.SUCCEEDED)
        client.exchange(HttpRequest.POST(WORKFLOW_TRACE_ENDPOINT, workflowCompleted), TraceWorkflowResponse.class)
    }

    private void sleepIfSet() {
        if (sleepBetweenRequests) {
            sleep(sleepBetweenRequests)
        }
    }

    private List<TraceTaskRequest> computeTaskTraceSequence(Long workflowId) {
        List<Integer> tasksOrders = TracesJsonBank.getUniqueTasksOrders(workflowOrder)

        List<TraceTaskRequest> taskSubmittedTraces = tasksOrders.collect { Integer taskOrder -> TracesJsonBank.extractTaskJsonTrace(workflowOrder, taskOrder, workflowId, TaskStatus.SUBMITTED) }
        List<TraceTaskRequest> taskRunningTraces = tasksOrders.collect { Integer taskOrder -> TracesJsonBank.extractTaskJsonTrace(workflowOrder, taskOrder, workflowId, TaskStatus.RUNNING) }
        List<TraceTaskRequest> taskCompletedTraces = tasksOrders.collect { Integer taskOrder -> TracesJsonBank.extractTaskJsonTrace(workflowOrder, taskOrder, workflowId, TaskStatus.COMPLETED) }

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

    static List<File> simulateNextflowWithTowerJsonSequence(Integer workflowOrder) {
        File workflowDir = new File("${RESOURCES_DIR_PATH}/workflow_${workflowOrder}/")

        List<File> jsonFiles = workflowDir.listFiles().toList().sort { it.name }

        File workflowStartedJsonFile = jsonFiles.find { it.name.startsWith('workflow') && it.name.contains(WorkflowStatus.STARTED.name().toLowerCase()) }
        File workflowCompletedJsonFile = jsonFiles.find { it.name.startsWith('workflow') && (it.name.contains(WorkflowStatus.SUCCEEDED.name().toLowerCase()) || it.name.contains(WorkflowStatus.FAILED.name().toLowerCase())) }

        List<File> taskSubmittedJsonFiles = jsonFiles.findAll { it.name.startsWith('task') && it.name.contains(TaskStatus.SUBMITTED.name().toLowerCase()) }
        List<File> taskRunningJsonFiles = jsonFiles.findAll { it.name.startsWith('task') && it.name.contains(TaskStatus.STARTED.name().toLowerCase()) }
        List<File> taskCompletedJsonFiles = jsonFiles.findAll { it.name.startsWith('task') && (it.name.contains(TaskStatus.SUCCEEDED.name().toLowerCase()) || it.name.contains(TaskStatus.FAILED.name().toLowerCase())) }

        List<File> jsonSequence = [workflowStartedJsonFile]

        Random random = new Random()
        int nTakenElementsSubmitted = 0, nTakenElementsRunning = 0, nTakenElementsCompleted = 0
        while (!taskSubmittedJsonFiles.isEmpty() || !taskRunningJsonFiles.isEmpty() || !taskCompletedJsonFiles.isEmpty()) {
            int maxElementsSubmittedToTake = random.nextInt(taskSubmittedJsonFiles.size() + 1)
            maxElementsSubmittedToTake.times {
                File jsonFile = taskSubmittedJsonFiles.first()
                taskSubmittedJsonFiles.remove(0)
                jsonSequence << jsonFile
                nTakenElementsSubmitted++
            }

            int maxElementsRunningToTake = random.nextInt(taskRunningJsonFiles.size() + 1)
            maxElementsRunningToTake.times {
                boolean canTake = (nTakenElementsRunning < nTakenElementsSubmitted)
                if (canTake) {
                    File jsonFile = taskRunningJsonFiles.first()
                    taskRunningJsonFiles.remove(0)
                    jsonSequence << jsonFile
                    nTakenElementsRunning++
                }
            }

            int maxElementsCompletedToTake = random.nextInt(taskCompletedJsonFiles.size() + 1)
            maxElementsCompletedToTake.times {
                boolean canTake = (nTakenElementsCompleted < nTakenElementsRunning)
                if (canTake) {
                    File jsonFile = taskCompletedJsonFiles.first()
                    taskCompletedJsonFiles.remove(0)
                    jsonSequence << jsonFile
                    nTakenElementsCompleted++
                }
            }
        }
        jsonSequence << workflowCompletedJsonFile

        jsonSequence
    }

}

package io.seqera.watchtower.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.seqera.watchtower.controller.TraceWorkflowRequest
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.enums.WorkflowStatus

class TracesJsonBank {

    private final static RESOURCES_DIR_PATH = 'src/test/resources'

    static TraceWorkflowRequest extractWorkflowJsonTrace(Integer workflowOrder, Long workflowId, WorkflowStatus workflowStatus) {
        String fileRelativePath = "workflow_${workflowOrder}/workflow_${workflowStatus.name().toLowerCase()}.json"
        File jsonFile = new File("${RESOURCES_DIR_PATH}/${fileRelativePath}")

        TraceWorkflowRequest workflowTrace = new ObjectMapper().readValue(jsonFile, TraceWorkflowRequest.class)
        workflowTrace.workflow.workflowId = workflowId

        workflowTrace
    }

    static Map extractTaskJsonTrace(Integer workflowOrder, Integer taskOrder, Long workflowId, TaskStatus taskStatus) {
        String fileRelativePath = "workflow_${workflowOrder}/task_${taskOrder}_${taskStatus.name().toLowerCase()}.json"
        File jsonFile = new File("${RESOURCES_DIR_PATH}/${fileRelativePath}")

        Map jsonTrace = new ObjectMapper().readValue(jsonFile, HashMap.class)
        jsonTrace.task.workflowId = workflowId

        jsonTrace
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

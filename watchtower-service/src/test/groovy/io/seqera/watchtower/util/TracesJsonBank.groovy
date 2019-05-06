package io.seqera.watchtower.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.enums.WorkflowStatus

class TracesJsonBank {

    private final static RESOURCES_DIR_PATH = 'src/test/resources'

    static Map extractWorkflowJsonTrace(Integer workflowOrder, Long workflowId, WorkflowStatus workflowStatus) {
        String fileRelativePath = "workflow_${workflowOrder}/workflow_${workflowStatus.toString().toLowerCase()}.json"
        File jsonFile = new File("${RESOURCES_DIR_PATH}/${fileRelativePath}")

        Map jsonTrace = new ObjectMapper().readValue(jsonFile, HashMap.class)
        jsonTrace.workflow.workflowId = workflowId

        jsonTrace
    }

    static Map extractTaskJsonTrace(Integer workflowOrder, Integer taskOrder, Long workflowId, TaskStatus taskStatus) {
        String fileRelativePath = "workflow_${workflowOrder}/task_${taskOrder}_${taskStatus.toString().toLowerCase()}.json"
        File jsonFile = new File("${RESOURCES_DIR_PATH}/${fileRelativePath}")

        Map jsonTrace = new ObjectMapper().readValue(jsonFile, HashMap.class)
        jsonTrace.task.workflowId = workflowId

        jsonTrace
    }

}

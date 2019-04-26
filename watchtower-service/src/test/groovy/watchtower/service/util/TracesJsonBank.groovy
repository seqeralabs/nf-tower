package watchtower.service.util

import com.fasterxml.jackson.databind.ObjectMapper
import watchtower.service.pogo.enums.TaskStatus
import watchtower.service.pogo.enums.WorkflowStatus

class TracesJsonBank {

    private final static RESOURCES_DIR_PATH = 'src/test/resources'

    static Map extractWorkflowJsonTrace(Integer workflowId, WorkflowStatus workflowStatus) {
        String fileRelativePath = "workflow_${workflowId}/workflow_${workflowStatus.toString().toLowerCase()}.json"
        File jsonFile = new File("${RESOURCES_DIR_PATH}/${fileRelativePath}")

        new ObjectMapper().readValue(jsonFile, HashMap.class)
    }

    static Map extractTaskJsonTrace(Integer workflowId, Integer taskId, TaskStatus taskStatus) {
        String fileRelativePath = "workflow_${workflowId}/task_${taskId}_${taskStatus.toString().toLowerCase()}.json"
        File jsonFile = new File("${RESOURCES_DIR_PATH}/${fileRelativePath}")

        new ObjectMapper().readValue(jsonFile, HashMap.class)
    }

}

package watchtower.service.pogo

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import watchtower.service.domain.Task
import watchtower.service.pogo.enums.TaskStatus

import java.time.Instant

@CompileStatic
class TaskTraceJsonUnmarshaller {

    static TaskStatus identifyTaskStatus(Map workflowJson) {
        TaskStatus taskStatus = null

        if (workflowJson.event == 'process_submitted') {
            taskStatus = TaskStatus.SUBMITTED
        } else if (workflowJson.event == 'process_started') {
            taskStatus = TaskStatus.STARTED
        } else if (workflowJson.event == 'process_completed') {
            taskStatus = (workflowJson.trace['status'] == 'FAILED') ? TaskStatus.FAILED : TaskStatus.SUCCEEDED
        }

        taskStatus
    }

    @CompileDynamic
    static void populateTaskFields(Map<String, Object> taskJson, TaskStatus taskStatus, Task task) {
        task.currentStatus = taskStatus
        taskJson.trace.each { String k, def v ->
            if (k == 'module') {
                task[k] = new ObjectMapper().writeValueAsString(v)
            } else if (k == 'submit' || k == 'start' || k == 'complete') {
                task."${k}Time" = v ? Instant.ofEpochMilli(v) : null
            }   else {
                task[k] = v
            }
        }
    }

}

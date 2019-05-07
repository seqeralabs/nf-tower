package io.seqera.watchtower.pogo

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.pogo.enums.TaskStatus

import java.time.Instant

class TaskTraceJsonUnmarshaller {

    static TaskStatus identifyTaskStatus(Map taskJson) {
        TaskStatus taskStatus = null

        if (taskJson.task['status'] == 'SUBMITTED') {
            taskStatus = TaskStatus.SUBMITTED
        } else if (taskJson.task['status'] == 'RUNNING') {
            taskStatus = TaskStatus.STARTED
        } else if (taskJson.task['status'] == 'COMPLETED') {
            taskStatus = (taskJson.task['errorAction']) ? TaskStatus.FAILED : TaskStatus.SUCCEEDED
        }
        
        taskStatus
    }

    @CompileDynamic
    static void populateTaskFields(Map<String, Object> taskJson, TaskStatus taskStatus, Task task) {
        task.currentStatus = taskStatus
        taskJson.task.each { String k, def v ->
            if (k == 'module') {
                task[k] = new ObjectMapper().writeValueAsString(v)
            } else if (k == 'submit' || k == 'start' || k == 'complete') {
                task."${k}Time" = v ? Instant.ofEpochMilli(v) : null
            }   else if (!isIgnoredField(k, task)) {
                task[k] = v
            }
        }
    }

    private static boolean isIgnoredField(String key, Task task) {
        !task.hasProperty(key) || key == 'workflowId'
    }

}

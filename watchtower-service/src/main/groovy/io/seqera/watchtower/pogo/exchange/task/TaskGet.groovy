package io.seqera.watchtower.pogo.exchange.task

import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.Task

class TaskGet {

    Task task
    Progress progress

    static TaskGet of(Task task) {
        new TaskGet(task: task, progress: task.workflow.progress)
    }
}

package io.seqera.watchtower.pogo.exchange.trace.sse

import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.SseErrorType
import io.seqera.watchtower.pogo.exchange.task.TaskGet
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet

class TraceSseResponse {

    WorkflowGet workflow
    TaskGet task
    SseError error


    static TraceSseResponse ofWorkflow(Workflow workflow) {
        new TraceSseResponse(workflow: WorkflowGet.of(workflow))
    }

    static TraceSseResponse ofTask(Task task) {
        new TraceSseResponse(task: TaskGet.of(task))
    }

    static TraceSseResponse ofError(SseErrorType type, String errorMessage) {
        SseError sseError = new SseError(type: type, message: errorMessage)

        new TraceSseResponse(error: sseError)
    }

}

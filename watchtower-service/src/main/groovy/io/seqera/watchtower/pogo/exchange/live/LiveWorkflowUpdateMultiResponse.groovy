package io.seqera.watchtower.pogo.exchange.live

import io.seqera.watchtower.pogo.exchange.task.TaskGet
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet

class LiveWorkflowUpdateMultiResponse {

    WorkflowGet workflow
    TaskGet task
    String error

}

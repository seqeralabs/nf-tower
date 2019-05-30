package io.seqera.watchtower.pogo.exchange.workflow

class WorkflowList {

    List<WorkflowGet> workflows

    static WorkflowList of(List<WorkflowGet> workflows) {
        new WorkflowList(workflows: workflows)
    }
}

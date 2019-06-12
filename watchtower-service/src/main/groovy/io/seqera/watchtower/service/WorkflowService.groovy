package io.seqera.watchtower.service

import io.seqera.watchtower.domain.User
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest

interface WorkflowService {

    Workflow get(Serializable id)

    List<Workflow> list(User owner)

    void delete(Workflow workflow)

    Workflow processWorkflowJsonTrace(TraceWorkflowRequest traceWorkflowRequest, User owner)

}
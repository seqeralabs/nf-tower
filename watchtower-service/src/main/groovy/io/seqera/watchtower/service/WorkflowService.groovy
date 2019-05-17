package io.seqera.watchtower.service

import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest

interface WorkflowService {

    Workflow get(Serializable id)

    List<Workflow> list()

    Workflow processWorkflowJsonTrace(TraceWorkflowRequest traceWorkflowRequest)

}
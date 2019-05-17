package io.seqera.watchtower.service

import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest

interface TraceService {

    Workflow processWorkflowTrace(TraceWorkflowRequest traceJson)

    Task processTaskTrace(TraceTaskRequest traceJson)

}


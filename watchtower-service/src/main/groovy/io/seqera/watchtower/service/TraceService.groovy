package io.seqera.watchtower.service

import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse

interface TraceService {

    TraceWorkflowResponse processWorkflowTrace(TraceWorkflowRequest traceJson)

    TraceTaskResponse processTaskTrace(TraceTaskRequest traceJson)

}


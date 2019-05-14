package io.seqera.watchtower.service

import io.seqera.watchtower.controller.TraceWorkflowRequest
import io.seqera.watchtower.controller.TraceWorkflowResponse

interface TraceService {

    TraceWorkflowResponse createEntityByTrace(TraceWorkflowRequest traceJson)

    TraceWorkflowResponse processWorkflowTrace(TraceWorkflowRequest traceJson)

    TraceWorkflowResponse processTaskTrace(TraceWorkflowRequest traceJson)

}


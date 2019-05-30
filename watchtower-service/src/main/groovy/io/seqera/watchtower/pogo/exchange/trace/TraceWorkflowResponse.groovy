package io.seqera.watchtower.pogo.exchange.trace

import groovy.transform.ToString
import io.seqera.watchtower.pogo.enums.TraceProcessingStatus

/**
 * Model a Trace workflow response
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceWorkflowResponse {

    TraceProcessingStatus status
    String message
    String workflowId

    static TraceWorkflowResponse ofSuccess(String workflowId) {
        new TraceWorkflowResponse(status: TraceProcessingStatus.OK, workflowId: workflowId)
    }

    static TraceWorkflowResponse ofError(String message) {
        new TraceWorkflowResponse(status: TraceProcessingStatus.KO, message: message)
    }

}

package io.seqera.watchtower.pogo.exchange.trace

import groovy.transform.ToString
import io.seqera.watchtower.pogo.enums.TraceProcessingStatus

/**
 * Model a Trace workflow response
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceTaskResponse {

    TraceProcessingStatus status
    String message
    String workflowId


    static TraceTaskResponse ofSuccess(String workflowId) {
        new TraceTaskResponse(status: TraceProcessingStatus.OK, workflowId: workflowId)
    }

    static TraceTaskResponse ofError(String message) {
        new TraceTaskResponse(status: TraceProcessingStatus.KO, message: message)
    }

}

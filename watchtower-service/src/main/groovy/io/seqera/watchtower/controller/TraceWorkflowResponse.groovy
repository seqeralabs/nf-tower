package io.seqera.watchtower.controller

import groovy.transform.ToString
import io.seqera.watchtower.pogo.enums.TraceType

/**
 * Model a Trace workflow response
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceWorkflowResponse {

    String status
    String message
    String workflowId
    TraceType traceType
}

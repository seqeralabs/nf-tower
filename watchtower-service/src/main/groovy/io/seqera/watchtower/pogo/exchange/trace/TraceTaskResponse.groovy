package io.seqera.watchtower.pogo.exchange.trace

import groovy.transform.ToString

/**
 * Model a Trace workflow response
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceTaskResponse {

    String status
    String message
    String workflowId

}

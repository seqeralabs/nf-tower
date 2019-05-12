package io.seqera.watchtower.controller

import groovy.transform.ToString

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
}

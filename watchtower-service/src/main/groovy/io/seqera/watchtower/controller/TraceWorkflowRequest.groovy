package io.seqera.watchtower.controller

import java.time.Instant

import groovy.transform.ToString
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.WorkflowObj
/**
 * Model a Trace workflow request
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceWorkflowRequest {

    WorkflowObj workflow
    Instant utcTime
    Progress progress
    Map summary

}

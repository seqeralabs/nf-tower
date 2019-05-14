package io.seqera.watchtower.controller

import com.fasterxml.jackson.annotation.JsonSetter
import io.seqera.watchtower.domain.Workflow

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

    Workflow workflow
    Instant utcTime
    Progress progress
    Map summary

    @JsonSetter('utcTime')
    void deserializeCompleteInstant(String utcTimestamp) {
        utcTime = utcTimestamp ? Instant.parse(utcTimestamp) : null
    }

}

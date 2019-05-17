package io.seqera.watchtower.pogo.exchange.trace

import com.fasterxml.jackson.annotation.JsonSetter
import groovy.transform.ToString
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.SummaryEntry
import io.seqera.watchtower.domain.Workflow

import java.time.Instant

/**
 * Model a Trace workflow request
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceWorkflowRequest {

    Workflow workflow
    Progress progress
    List<SummaryEntry> summary

    Instant utcTime


    @JsonSetter('utcTime')
    void deserializeCompleteInstant(String utcTimestamp) {
        utcTime = utcTimestamp ? Instant.parse(utcTimestamp) : null
    }

}

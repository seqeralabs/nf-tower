package io.seqera.watchtower.pogo.exchange.trace

import com.fasterxml.jackson.annotation.JsonSetter
import groovy.transform.ToString
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.Task

import java.time.Instant

/**
 * Model a Trace workflow request
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceTaskRequest {

    Task task
    Progress progress

    Instant utcTime


    @JsonSetter('utcTime')
    void deserializeCompleteInstant(String utcTimestamp) {
        utcTime = utcTimestamp ? Instant.parse(utcTimestamp) : null
    }

}

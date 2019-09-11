/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.service

import javax.annotation.PostConstruct
import javax.inject.Singleton
import java.time.Duration
import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.sse.Event
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.seqera.tower.exchange.trace.sse.TraceSseResponse

@Singleton
@Slf4j
@CompileStatic
class ServerSentEventsServiceImpl implements ServerSentEventsService {

    private final PublishProcessor<TraceSseResponse> eventPublisher = PublishProcessor.create()
    private Flowable<Event<List<TraceSseResponse>>> eventFlowable

    @Value('${sse.buffer.time:1s}')
    Duration bufferTimeout

    @Value('${sse.buffer.count:100}')
    Integer bufferCount

    @Value('${sse.buffer.heartbeat:1m}')
    Duration heartbeatDuration

    @PostConstruct
    void initialize() {
        eventFlowable = createBufferedEventFlowable()
    }


    Flowable<Event<List<TraceSseResponse>>> getEventsFlowable() {
        return eventFlowable
    }

    void publishEvent(TraceSseResponse traceSseResponse) {
        log.trace("Publishing event=${traceSseResponse.toString()}")
        eventPublisher.onNext(traceSseResponse)
    }

    private Flowable<Event<List<TraceSseResponse>>> createBufferedEventFlowable() {
        log.debug "Creating SSE event buffer flowable timeout=$bufferTimeout count=$bufferCount heartbeat=$heartbeatDuration"
        
        return eventPublisher
                    // group together all events in a window of one second (up to 100)
                    .buffer(bufferTimeout.toMillis(), TimeUnit.MILLISECONDS, bufferCount)
                    // remove all identical events
                    .map({ List<TraceSseResponse> traces -> traces.unique() })
                    // discard empty events
                    .filter({ List<TraceSseResponse> traces -> traces.size()>0 })
                    // the following buffer behaves as a heartbeat
                    // the count=1 makes pass any trace event
                    // if not trace event show within the `heartbeatDuration` it emits an empty event
                    .buffer(heartbeatDuration.toMillis(), TimeUnit.MILLISECONDS, 1)
                    // finally wrap the traces in a `Event` type
                    // note this guy gets a list of list (!)
                    .map { List<List<TraceSseResponse>> wrap ->
                        final List<TraceSseResponse> traces = wrap ? wrap.first() : Collections.<TraceSseResponse>emptyList()
                        if( log.isTraceEnabled() )
                            log.trace "Send SSE events: ${traces.toString()})"
                        else
                            log.debug "Send SSE events (count=${traces.size()})"
                        Event.of(traces)
                    }

    }

}

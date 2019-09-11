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
import io.seqera.tower.exchange.live.LiveUpdate

@Singleton
@Slf4j
@CompileStatic
class LiveEventsServiceImpl implements LiveEventsService {

    private final PublishProcessor<LiveUpdate> eventPublisher = PublishProcessor.create()
    private Flowable<Event<List<LiveUpdate>>> eventFlowable

    @Value('${live.buffer.time:1s}')
    Duration bufferTimeout

    @Value('${live.buffer.count:100}')
    Integer bufferCount

    @Value('${live.buffer.heartbeat:1m}')
    Duration heartbeatDuration

    @PostConstruct
    void initialize() {
        eventFlowable = createBufferedEventFlowable()
    }


    Flowable<Event<List<LiveUpdate>>> getEventsFlowable() {
        return eventFlowable
    }

    void publishEvent(LiveUpdate traceSseResponse) {
        log.trace("Publishing event=${traceSseResponse.toString()}")
        eventPublisher.onNext(traceSseResponse)
    }

    private Flowable<Event<List<LiveUpdate>>> createBufferedEventFlowable() {
        log.info "Creating SSE event buffer flowable timeout=$bufferTimeout count=$bufferCount heartbeat=$heartbeatDuration"
        
        return eventPublisher
                    // group together all events in a window of one second (up to 100)
                    .buffer(bufferTimeout.toMillis(), TimeUnit.MILLISECONDS, bufferCount)
                    // remove all identical events
                    .map({ List<LiveUpdate> traces -> traces.unique() })
                    // discard empty events
                    .filter({ List<LiveUpdate> traces -> traces.size()>0 })
                    // the following buffer behaves as a heartbeat
                    // the count=1 makes pass any trace event
                    // if not trace event show within the `heartbeatDuration` it emits an empty event
                    .buffer(heartbeatDuration.toMillis(), TimeUnit.MILLISECONDS, 1)
                    // finally wrap the traces in a `Event` type
                    // note this guy gets a list of list (!)
                    .map { List<List<LiveUpdate>> wrap ->
                        final List<LiveUpdate> traces = wrap ? wrap.first() : Collections.<LiveUpdate>emptyList()
                        if( log.isTraceEnabled() )
                            log.trace "Send SSE events: ${traces.toString()})"
                        Event.of(traces)
                    }

    }

}

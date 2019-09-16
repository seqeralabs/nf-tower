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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.sse.Event
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.seqera.tower.exchange.live.LiveUpdate
import io.seqera.tower.util.BackpressureBuffer

@Singleton
@Slf4j
@CompileStatic
class LiveEventsServiceImpl implements LiveEventsService {

    private final PublishProcessor<List<LiveUpdate>> eventPublisher = PublishProcessor.create()

    private Flowable<Event<List<LiveUpdate>>> eventFlowable

    @Value('${live.buffer.time:1s}')
    Duration bufferTimeout

    @Value('${live.buffer.count:100}')
    Integer bufferCount

    @Value('${live.buffer.heartbeat:1m}')
    Duration heartbeatDuration

    BackpressureBuffer buffer

    @PostConstruct
    void initialize() {
        log.info "Creating SSE event buffer flowable timeout=$bufferTimeout count=$bufferCount heartbeat=$heartbeatDuration"

        // -- implements the back pressure logic
        buffer = new BackpressureBuffer<LiveUpdate>()
                .setTimeout(bufferTimeout)
                .setHeartbeat(heartbeatDuration)
                .setMaxCount(bufferCount)
                .onNext { List<LiveUpdate> updates ->
                    log.debug "Publishing live updates -> (${updates.size()}) ${updates}"
                    eventPublisher.onNext(updates)
                }
                .start()

        // -- wrap into a Event object
        eventFlowable = eventPublisher
                .map { List<LiveUpdate> traces ->
                    log.trace "Publisher map traces (${traces.size()}) $traces"
                    Event.of(traces)
                }
    }


    Flowable<Event<List<LiveUpdate>>> getEventsFlowable() {
        return eventFlowable
    }

    void publishEvent(LiveUpdate liveUpdate) {
        buffer.offer(liveUpdate)
    }

    void stop() {
        buffer?.terminateAndAwait()
    }

}

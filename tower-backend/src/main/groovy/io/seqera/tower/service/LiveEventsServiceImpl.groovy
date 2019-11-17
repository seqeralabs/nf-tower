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
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.LiveAction
import io.seqera.tower.exchange.live.LiveUpdate
import io.seqera.tower.util.BackpressureBuffer
import org.reactivestreams.Publisher

@Singleton
@Slf4j
@CompileStatic
class LiveEventsServiceImpl implements LiveEventsService {

    @Value('${live.buffer.time:1s}')
    Duration bufferTimeout

    @Value('${live.buffer.count:100}')
    Integer bufferCount

    @Value('${live.buffer.heartbeat:1m}')
    Duration heartbeatDuration

    BackpressureBuffer<LiveUpdate> buffer

    PublishProcessor<List<LiveUpdate>> eventProcessor

    Flowable<Event<List<LiveUpdate>>> eventPublisher

    @PostConstruct
    void initialize() {
        log.info "Creating SSE event buffer flowable timeout=$bufferTimeout count=$bufferCount heartbeat=$heartbeatDuration"

        eventProcessor = PublishProcessor.create()

        eventPublisher = eventProcessor
                        .map { List<LiveUpdate> traces ->
                            log.trace "Publishing map traces (${traces.size()}) $traces"
                            Event.of(traces)
                        }

        // -- implements the back pressure logic
        buffer = new BackpressureBuffer<LiveUpdate>()
                .setName('Live events buffer')
                .setTimeout(bufferTimeout)
                .setHeartbeat(heartbeatDuration)
                .setMaxCount(bufferCount)
                .onNext { List<LiveUpdate> updates ->
                    log.trace "Publishing live updates -> (${updates.size()}) ${updates}"
                    eventProcessor.onNext(updates)
                }
                .start()
    }

    @Override
    void publishWorkflowEvent(Workflow workflow) {
        publishEvent(LiveUpdate.of(workflow.owner.id, workflow.id, LiveAction.WORKFLOW_UPDATE));
    }

    @Override
    void publishProgressEvent(Workflow workflow) {
        publishEvent(LiveUpdate.of(workflow.owner.id, workflow.id, LiveAction.PROGRESS_UPDATE));
    }

    @Override
    Publisher<Event<List<LiveUpdate>>> getEventPublisher() {
        // to avoid the response to stall when no events are emitted
        // merge the publisher with an empty event
        return eventPublisher
                .mergeWith( Flowable.just(Event.of(Collections.emptyList())) )
    }

    void publishEvent(LiveUpdate liveUpdate) {
        buffer.offer(liveUpdate)
    }

    void stop() {
        buffer?.terminateAndAwait()
    }

}

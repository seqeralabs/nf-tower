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
    Duration bufferFlowableTime
    @Value('${sse.buffer.count:100}')
    Integer bufferFlowableCount

    @PostConstruct
    void initialize() {
        eventFlowable = createBufferedEventFlowable()
    }


    Flowable<Event<List<TraceSseResponse>>> getEventsFlowable() {
        return eventFlowable
    }

    void publishEvent(TraceSseResponse traceSseResponse) {
        log.debug("Publishing event for [userId - ${traceSseResponse.userId}] [workflowId - ${traceSseResponse.workflowId}]")
        eventPublisher.onNext(traceSseResponse)
    }

    private Flowable<Event<List<TraceSseResponse>>> createBufferedEventFlowable() {
        return eventPublisher.buffer(bufferFlowableTime.toMillis(), TimeUnit.MILLISECONDS, bufferFlowableCount)
                             .map({ List<TraceSseResponse> traces -> Event.of(traces.unique()) })
    }

}

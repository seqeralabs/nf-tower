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

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Prototype
import io.micronaut.http.sse.Event
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.reactivex.processors.PublishProcessor
import io.seqera.tower.exceptions.NonExistingFlowableException

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Prototype
@Slf4j
class ServerSentEventsServiceImpl implements ServerSentEventsService {

    private final Map<String, PublishProcessor<Event>> flowableByKeyCache = new ConcurrentHashMap()


    Flowable getOrCreate(String key) {
        log.info("Creating flowable: ${key}")
        if( flowableByKeyCache.containsKey(key) )
            return flowableByKeyCache.get(key)

        synchronized (flowableByKeyCache) {
            if( flowableByKeyCache.containsKey(key) )
                return flowableByKeyCache.get(key)
            def result = PublishProcessor.<Event>create()
            // TODO create timeout
            flowableByKeyCache.put(key, result)
            return result
        }
    }

    void createFlowable(String key, Duration idleTimeout) {
        log.info("Creating flowable: ${key}")
        flowableByKeyCache[key] = PublishProcessor.create()
        scheduleFlowableIdleTimeout(key, idleTimeout)
    }

    private void scheduleFlowableIdleTimeout(String key, Duration idleTimeout) {
        Flowable flowable = getFlowable(key)

        Flowable timeoutFlowable = flowable.timeout(idleTimeout.toMillis(), TimeUnit.MILLISECONDS)
        timeoutFlowable.subscribe(
                {
                    log.info("Data published for flowable: ${key}")
                } as Consumer,
                { Throwable t ->
                    if (t instanceof TimeoutException) {
                        log.info("Idle timeout reached for flowable: ${key}")
                        completeFlowable(key)
                    } else {
                        log.info("Unexpected error happened for id: ${key} | ${t.message}")
                    }
                } as Consumer
        )
    }

    void publishEvent(String key, Event event) throws NonExistingFlowableException {
        log.info("Publishing event for flowable: ${key}")
        PublishProcessor hotFlowable = (PublishProcessor) getFlowable(key)

        hotFlowable.onNext(event)
    }

    void tryPublish(String key, Closure<Event> payload) {
        Flowable flowable = flowableByKeyCache.get(key)
        if( flowable ) {
            flowable.onNext(payload.call())
        }
    }

    void completeFlowable(String key) {
        log.info("Completing flowable: ${key}")
        PublishProcessor hotFlowable = (PublishProcessor) getFlowable(key)

        hotFlowable.onComplete()
        flowableByKeyCache.remove(key)
    }

    private Flowable getFlowable(String key) throws NonExistingFlowableException {
        Flowable hotFlowable = flowableByKeyCache[key]

        if (!hotFlowable) {
            throw new NonExistingFlowableException("No flowable exists for id: ${key}")
        }

        hotFlowable
    }

    Flowable getThrottledFlowable(String key, Duration throttleTime) throws NonExistingFlowableException {
        Flowable flowable = getFlowable(key)

        flowable.throttleLatest(throttleTime.toMillis(), TimeUnit.MILLISECONDS, true)
    }

    Flowable generateHeartbeatFlowable(Duration interval, Closure<Event> heartbeatEventGenerator) {
        Flowable.interval(interval.toMillis(), TimeUnit.MILLISECONDS)
                .map(heartbeatEventGenerator)
    }

}

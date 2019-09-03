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

import javax.inject.Singleton
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Singleton
@Slf4j
class ServerSentEventsServiceImpl implements ServerSentEventsService {

    private final Map<String, PublishProcessor<Event>> flowableByKeyCache = new ConcurrentHashMap()


    Flowable getOrCreate(String key, Duration idleTimeout, Duration throttleTime) {
        synchronized (flowableByKeyCache) {
            if(flowableByKeyCache.containsKey(key)) {
                log.info("Getting flowable: ${key}")
                return flowableByKeyCache[key]
            }

            log.info("Creating flowable: ${key}")
            Flowable<Event> flowable = PublishProcessor.<Event>create()
            flowableByKeyCache[key] = flowable

            scheduleFlowableIdleTimeout(key, idleTimeout)
            if (throttleTime) {
                flowable = flowable.throttleLatest(throttleTime.toMillis(), TimeUnit.MILLISECONDS, true)
            }

            return flowable
        }
    }

    private void scheduleFlowableIdleTimeout(String key, Duration idleTimeout) {
        Flowable flowable = flowableByKeyCache[key]

        Flowable timeoutFlowable = flowable.timeout(idleTimeout.toMillis(), TimeUnit.MILLISECONDS)
        timeoutFlowable.subscribe(
            {
                log.info("Data published for flowable: ${key}")
            } as Consumer,
            { Throwable t ->
                if (t instanceof TimeoutException) {
                    log.info("Idle timeout reached for flowable: ${key}")
                    tryComplete(key)
                } else {
                    log.info("Unexpected error happened for id: ${key} | ${t.message}")
                }
            } as Consumer
        )
    }

    void tryPublish(String key, Closure<Event> payload) {
        Flowable flowable = flowableByKeyCache[key]
        if (flowable) {
            log.info("Publishing event for flowable: ${key}")
            flowable.onNext(payload.call())
        }
    }

    void tryComplete(String key) {
        PublishProcessor flowable = flowableByKeyCache[key]
        if (flowable) {
            log.info("Completing flowable: ${key}")
            flowable.onComplete()
            flowableByKeyCache.remove(key)
        }
    }

    Flowable generateHeartbeatFlowable(Duration interval, Closure<Event> heartbeatEventGenerator) {
        Flowable.interval(interval.toMillis(), TimeUnit.MILLISECONDS)
                .map(heartbeatEventGenerator)
    }

}

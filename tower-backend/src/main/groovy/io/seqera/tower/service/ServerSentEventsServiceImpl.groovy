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

import javax.inject.Singleton
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.sse.Event
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.functions.Consumer
import io.reactivex.processors.PublishProcessor
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.SseErrorType
import io.seqera.tower.exchange.trace.sse.TraceSseResponse

@Singleton
@Slf4j
@CompileStatic
class ServerSentEventsServiceImpl implements ServerSentEventsService {

    private final Map<String, PublishProcessor<Event>> publisherByKeyCache = new ConcurrentHashMap(20)
    private final Map<PublishProcessor<Event>, Flowable<Event>> heartbeats = new ConcurrentHashMap<>(20)

    @Value('${sse.time.idle.workflow:5m}')
    Duration idleWorkflowFlowableTimeout
    @Value('${sse.time.idle.user:5m}')
    Duration idleUserFlowableTimeout
    @Value('${sse.time.heartbeat.user:1m}')
    Duration heartbeatUserFlowableInterval


    Flowable<Event> getOrCreateUserPublisher(Serializable userId) {
        String userFlowableKey = getKeyForEntity(User, userId)

        getOrCreatePublisher(userFlowableKey, idleUserFlowableTimeout, { Event.of(TraceSseResponse.ofError(SseErrorType.TIMEOUT, "Expired [${userFlowableKey}]")) })
        Flowable<Event> userFlowableWithHeartbeat = getOrCreateHeartbeatForPublisher(publisherByKeyCache[userFlowableKey]) {
            log.debug("Server heartbeat ${it} generated for flowable: ${userFlowableKey}")
            Event.of(TraceSseResponse.ofHeartbeat("Server heartbeat [${userFlowableKey}]"))
        }

        return userFlowableWithHeartbeat
    }

    Flowable getOrCreateWorkflowPublisher(Serializable workflowId) {
        final workflowFlowableKey = getKeyForEntity(Workflow, workflowId)
        final timeout = { Event.of(TraceSseResponse.ofError(SseErrorType.TIMEOUT, "Expired [${workflowFlowableKey}]")) }
        getOrCreatePublisher(workflowFlowableKey, idleWorkflowFlowableTimeout, timeout)
    }

    String getKeyForEntity(Class entityClass, def entityId) {
        "${entityClass.simpleName}-${entityId}"
    }

    Flowable<Event> getOrCreatePublisher(String key, Duration idleTimeout, Closure<Event> idleTimeoutLastEvent) {
        synchronized (publisherByKeyCache) {
            if(publisherByKeyCache.containsKey(key)) {
                log.trace("Getting flowable: ${key}")
                return publisherByKeyCache[key]
            }

            log.debug("Creating flowable: ${key}")
            Flowable<Event> flowable = PublishProcessor.<Event>create()
            publisherByKeyCache[key] = flowable

            scheduleFlowableIdleTimeout(key, idleTimeout, idleTimeoutLastEvent)

            return flowable
        }
    }

    private void scheduleFlowableIdleTimeout(String key, Duration idleTimeout, Closure<Event> idleTimeoutLastEventPayload) {
        final flowable = publisherByKeyCache[key]
        final timeoutFlowable = flowable.timeout(idleTimeout.toMillis(), TimeUnit.MILLISECONDS)

        final Consumer trace = { log.trace("Data published for flowable: ${key}") } as Consumer
        final Consumer handler = { Throwable t ->
            if (t instanceof TimeoutException) {
                log.debug("Idle timeout reached for flowable: ${key}")
                if (idleTimeoutLastEventPayload) {
                    tryPublish(key, idleTimeoutLastEventPayload)
                }
                tryComplete(key)
            }
            else {
                log.error("Unexpected error happened for id: ${key} | ${t.message}")
            }
        } as Consumer

        timeoutFlowable.subscribe(trace,handler)
    }


    void tryPublish(String key, Closure<Event> payload) {
        final flowable = publisherByKeyCache[key]
        if (flowable) {
            log.debug("Publishing event for flowable: ${key}")
            flowable.onNext(payload.call())
        }
    }

    void tryComplete(String key) {
        final flowable = publisherByKeyCache[key]
        if (flowable) {
            log.debug("Completing flowable: ${key}")
            flowable.onComplete()
            publisherByKeyCache.remove(key)
            heartbeats.remove(flowable)
        }
    }

    Flowable<Event> generateHeartbeatFlowable(Duration interval, Closure<Event> heartbeatEventGenerator) {
        Flowable.interval(interval.toMillis(), TimeUnit.MILLISECONDS)
                .map(heartbeatEventGenerator) as Flowable<Event>
    }

    Flowable<Event> getOrCreateHeartbeatForPublisher(PublishProcessor<Event> publisher, Closure<Event> heartbeatEventGenerator) {
        synchronized (heartbeats) {
            if (heartbeats.containsKey(publisher)) {
                return heartbeats.get(publisher)
            }

            ConnectableFlowable<Event> hotHeartbeatInterval = generateHeartbeatFlowable(heartbeatUserFlowableInterval, heartbeatEventGenerator)
                    .publish()
            Disposable heartbeatDispoable = hotHeartbeatInterval.connect()

            Flowable<Event> dataFlowableWithHeartbeats = publisher
                    .mergeWith(hotHeartbeatInterval)
                    .takeUntil(publisher.takeLast(1))
                    .doOnComplete({
                        heartbeatDispoable.dispose()
                    })

            heartbeats.put(publisher, dataFlowableWithHeartbeats)
            return dataFlowableWithHeartbeats
        }
    }

}

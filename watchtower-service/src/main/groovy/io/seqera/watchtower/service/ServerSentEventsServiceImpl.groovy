package io.seqera.watchtower.service

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Prototype
import io.micronaut.context.annotation.Value
import io.micronaut.http.sse.Event
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.reactivex.processors.PublishProcessor
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException

import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Prototype
@Slf4j
class ServerSentEventsServiceImpl implements ServerSentEventsService {

    private Map<String, PublishProcessor<Event>> flowableByKeyCache = new ConcurrentHashMap()

    @Value('${sse.idle.timeout:5m}')
    Duration idleFlowableTimeout

    @Value('${sse.throttle.time:0ms}')
    Duration throttleFlowableTime


    void createFlowable(String key) {
        log.info("Creating flowable: ${key}")

        flowableByKeyCache[key] = PublishProcessor.create()
        scheduleFlowableIdleTimeout(key)
    }

    void publishEvent(String key, Event event) throws NonExistingFlowableException {
        log.info("Publishing event for flowable: ${key}")
        PublishProcessor hotFlowable = (PublishProcessor) getFlowableInternal(key)

        hotFlowable.onNext(event)
    }

    void completeFlowable(String key) {
        log.info("Completing flowable: ${key}")
        PublishProcessor hotFlowable = (PublishProcessor) getFlowableInternal(key)

        hotFlowable.onComplete()
        flowableByKeyCache.remove(key)
    }

    private void scheduleFlowableIdleTimeout(String key) {
        Flowable flowable = getFlowableInternal(key)

        Flowable timeoutFlowable = flowable.timeout(idleFlowableTimeout.toMillis(), TimeUnit.MILLISECONDS)
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

    private Flowable getFlowableInternal(String key) throws NonExistingFlowableException {
        Flowable hotFlowable = flowableByKeyCache[key]

        if (!hotFlowable) {
            throw new NonExistingFlowableException("No flowable exists for id: ${key}")
        }

        hotFlowable
    }

    Flowable getFlowable(String key) throws NonExistingFlowableException {
        Flowable flowable = getFlowableInternal(key)

        flowable.throttleLatest(throttleFlowableTime.toMillis(), TimeUnit.MILLISECONDS, true)
    }

}

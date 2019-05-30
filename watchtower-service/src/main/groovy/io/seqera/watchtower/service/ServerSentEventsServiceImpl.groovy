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

    private Map<Long, PublishProcessor<Event>> flowableByIdCache = new ConcurrentHashMap()

    @Value('${sse.idle.timeout:5m}')
    Duration idleFlowableTimeout

    void createFlowable(Long id) {
        log.info("Creating flowable: ${id}")

        flowableByIdCache[id] = PublishProcessor.create()
        scheduleFlowableIdleTimeout(id)
    }

    void publishData(Long id, def data) {
        log.info("Publishing data for flowable: ${id}")
        PublishProcessor hotFlowable = (PublishProcessor) getFlowable(id)

        hotFlowable.onNext(Event.of(data))
    }

    void completeFlowable(Long id) {
        log.info("Completing flowable: ${id}")
        PublishProcessor hotFlowable = (PublishProcessor) getFlowable(id)

        hotFlowable.onComplete()
        flowableByIdCache.remove(id)
    }

    private void scheduleFlowableIdleTimeout(Long id) {
        Flowable flowable = getFlowable(id)

        Flowable timeoutFlowable = flowable.timeout(idleFlowableTimeout.toMillis(), TimeUnit.MILLISECONDS)
        timeoutFlowable.subscribe(
                {
                    log.info("Data published for flowable: ${id}")
                } as Consumer,
                { Throwable t ->
                    if (t instanceof TimeoutException) {
                        log.info("Idle timeout reached for flowable: ${id}")
                        completeFlowable(id)
                    } else {
                        log.info("Unexpected error happened for id: ${id} | ${t.message}")
                    }
                } as Consumer
        )
    }

    Flowable getFlowable(Long id) {
        Flowable hotFlowable = flowableByIdCache[id]

        if (!hotFlowable) {
            throw new NonExistingFlowableException("No flowable exists for id: ${id}")
        }

        hotFlowable
    }

}

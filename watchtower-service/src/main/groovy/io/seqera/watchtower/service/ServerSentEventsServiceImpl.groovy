package io.seqera.watchtower.service

import io.micronaut.http.sse.Event
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException

import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class ServerSentEventsServiceImpl implements ServerSentEventsService {

    private Map<Long, PublishProcessor<Event>> flowableByIdCache = new ConcurrentHashMap()


    void createFlowable(Long id) {
        flowableByIdCache[id] = PublishProcessor.create()
    }

    void publishUpdate(Long id, def data) {
        PublishProcessor hotFlowable = (PublishProcessor) getFlowable(id)

        hotFlowable.onNext(Event.of(data))
    }

    void completeFlowable(Long id) {
        PublishProcessor hotFlowable = (PublishProcessor) getFlowable(id)

        hotFlowable.onComplete()
        flowableByIdCache.remove(id)
    }

    Flowable getFlowable(Long id) {
        Flowable hotFlowable = flowableByIdCache[id]

        if (!hotFlowable) {
            throw new NonExistingFlowableException("No flowable for workflow ${id} exists")
        }

        hotFlowable
    }

}

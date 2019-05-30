package io.seqera.watchtower.service

import io.micronaut.http.sse.Event
import io.reactivex.Flowable

interface ServerSentEventsService {

    void createFlowable(String key)

    void publishEvent(String key, Event data)

    Flowable getFlowable(String key)

    void completeFlowable(String key)


}
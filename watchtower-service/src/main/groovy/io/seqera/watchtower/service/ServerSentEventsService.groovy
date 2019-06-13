package io.seqera.watchtower.service

import io.micronaut.http.sse.Event
import io.reactivex.Flowable

import java.time.Duration

interface ServerSentEventsService {

    void createFlowable(String key, Duration idleTimeout)

    void publishEvent(String key, Event data)

    Flowable getFlowable(String key, Duration throttleTime)

    void completeFlowable(String key)


}
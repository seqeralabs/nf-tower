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

import javax.inject.Inject
import java.time.Duration

import grails.gorm.transactions.Transactional
import io.micronaut.http.sse.Event
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest
import spock.lang.Unroll

@MicronautTest(application = Application.class)
@Transactional
class ServerSentEventsServiceTest extends AbstractContainerBaseTest {

    @Inject
    ServerSentEventsServiceImpl serverSentEventsService


    void "create a flowable given that it doesn't exist another one with the same key"() {
        given: 'a key for the flowable'
        String key = '1'

        when: 'create the flowable with the key'
        Flowable flowable = serverSentEventsService.getOrCreatePublisher(key, Duration.ofMinutes(1), null)

        and: 'get the same flowable'
        Flowable flowable2 = serverSentEventsService.getOrCreatePublisher(key, Duration.ofMinutes(1), null)

        then: 'both flowables are the same'
        flowable == flowable2
    }

    void "create a flowable and publish some data for it"() {
        given: 'a key for the flowable'
        String key = '2'

        and: 'create the flowable'
        Flowable flowable = serverSentEventsService.getOrCreatePublisher(key, Duration.ofMinutes(1), null)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = flowable.test()

        when: 'publish some data for it'
        Event event = Event.of([text: 'Data published'])
        serverSentEventsService.tryPublish(key) {
            event
        }

        then: 'the subscriber has obtained the data correctly'
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.text == 'Data published'
    }

    void "create a flowable and complete it"() {
        given: 'a key for the flowable'
        String key = '3'

        and: 'create the flowable'
        Flowable flowable = serverSentEventsService.getOrCreatePublisher(key, Duration.ofMinutes(1), null)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = flowable.test()

        when: 'complete the flowable'
        serverSentEventsService.tryComplete(key)

        then: 'the flowable has been completed'
        subscriber.assertComplete()

        when: 'try to get the flowable again'
        Flowable flowable2 = serverSentEventsService.getOrCreatePublisher(key, Duration.ofMinutes(0), null)

        then: 'the flowable is new'
        flowable != flowable2
    }


    @Unroll
    void "create a flowable and leave it idle until the timeout expires"() {
        given: 'a key for the flowable'
        String key = '5'

        and: 'set a short idle timeout'
        Duration idleTimeout = Duration.ofMillis(300)

        and: 'create the flowable'
        Flowable flowable = serverSentEventsService.getOrCreatePublisher(key, idleTimeout, lastEmission)

        and: 'subscribe to the flowable in order to retrieve data'
        TestSubscriber subscriber = flowable.test()

        when: 'sleep until the timeout plus a prudential time to make sure it was reached'
        sleep(idleTimeout.toMillis() + 100)

        then: 'check that a last emission was sent just before completing'
        subscriber.assertValueCount(lastEmission ? 1 : 0)
        subscriber.events?.getAt(0)?.getAt(0)?.data?.text == (lastEmission ? 'Timeout' : null)

        and: 'the flowable has been completed'
        subscriber.assertComplete()

        where: 'a last emission is sent or not'
        _ | lastEmission
        _ | null
        _ | { Event.of([text: 'Timeout']) }
    }

    void "try to publish data for a nonexistent flowable"() {
        given: 'a closure to check if the payload callback was called'
        boolean executed = false
        Closure payload = {
            executed = true
            Event.of([text: 'Data published 1'])
        }

        when: 'publish data for a nonexistent flowable'
        serverSentEventsService.tryPublish('nonExistent', payload)

        then: 'data was not published'
        !executed
    }

    void "create a heartbeat flowable and receive the heartbeat events"() {
        given: 'a heartbeat interval'
        Duration interval = Duration.ofMillis(250)

        and: 'the heatbeat flowable'
        Flowable heartbeatFlowable = serverSentEventsService.generateHeartbeatFlowable(interval, { Event.of([text: "Heartbeat ${it}"]) })

        when: 'subscribe to the flowable'
        TestSubscriber subscriber = heartbeatFlowable.test()

        and: 'sleep a prudential time to generate a heartbeat'
        sleep(interval.toMillis() + 50)

        then: 'the data has been generated'
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.text == 'Heartbeat 0'

        and: 'sleep a prudential time to generate another heartbeat'
        sleep(interval.toMillis() + 50)

        then: 'the data has been generated'
        subscriber.assertValueCount(2)
        subscriber.events.first()[1].data.text == 'Heartbeat 1'
    }

}

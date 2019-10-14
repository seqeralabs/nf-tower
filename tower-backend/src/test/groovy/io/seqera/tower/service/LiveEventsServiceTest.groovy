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

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.subscribers.TestSubscriber
import io.seqera.tower.Application
import io.seqera.tower.enums.LiveAction
import io.seqera.tower.exchange.live.LiveUpdate
import io.seqera.tower.util.AbstractContainerBaseTest
import spock.lang.Ignore

@MicronautTest(application = Application.class)
@Transactional
class LiveEventsServiceTest extends AbstractContainerBaseTest {

    @Inject
    LiveEventsServiceImpl liveEventsService


    void "publish a single element, the element is received after the time window passes"() {
        given: 'a trace to publish'
        LiveUpdate trace = LiveUpdate.of(1, '1', LiveAction.WORKFLOW_UPDATE)

        and: 'subscribe to the events stream'
        TestSubscriber subscriber = liveEventsService.eventPublisher.test()

        when: 'publish the trace'
        liveEventsService.publishEvent(trace)

        then: 'after the event is published, the data is no received right away'
        subscriber.assertValueCount(0)

        and: 'the event is received after the buffer time window passes'
        sleep(liveEventsService.bufferTimeout.toMillis() + 100) // --> Sleep the time window and add a prudential time to make sure the data has been received
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.userId == [trace.userId]
        subscriber.events.first()[0].data.workflowId == [trace.workflowId]
    }

    @Ignore
    void "publish as many elements as the buffer size windows, the elements are received"() {
        given: 'several traces to publish'
        List<LiveUpdate> traces = (1..liveEventsService.bufferCount).collect {
            LiveUpdate.of(it, it.toString(), LiveAction.WORKFLOW_UPDATE)
        }

        and: 'more traces overflowing the buffer'
        Integer overflow = liveEventsService.bufferCount.intdiv(2)
        (1..overflow).each {
            LiveUpdate.of(it, it.toString(), LiveAction.WORKFLOW_UPDATE)
        }

        and: 'subscribe to the events stream'
        TestSubscriber subscriber = liveEventsService.eventPublisher.test()

        when: 'publish all the traces'
        traces.each {
            liveEventsService.publishEvent(it)
        }

        then: 'the data is received right away and the buffer contains just as many elements as the size window'
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.userId == (1..liveEventsService.bufferCount)
        subscriber.events.first()[0].data.workflowId == (1..liveEventsService.bufferCount)*.toString()
    }

}

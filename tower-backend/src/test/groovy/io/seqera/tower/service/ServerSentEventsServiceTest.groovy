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
import io.seqera.tower.enums.WorkflowAction
import io.seqera.tower.exchange.trace.sse.TraceSseResponse
import io.seqera.tower.util.AbstractContainerBaseTest

@MicronautTest(application = Application.class)
@Transactional
class ServerSentEventsServiceTest extends AbstractContainerBaseTest {

    @Inject
    ServerSentEventsServiceImpl serverSentEventsService


    void "publish a single element, the element is received after the time window passes"() {
        given: 'a trace to publish'
        TraceSseResponse trace = TraceSseResponse.of(1, '1', WorkflowAction.WORKFLOW_UPDATE)

        and: 'subscribe to the events stream'
        TestSubscriber subscriber = serverSentEventsService.eventsFlowable.test()

        when: 'publish the trace'
        serverSentEventsService.publishEvent(trace)

        then: 'after the event is published, the data is no received right away'
        subscriber.assertValueCount(0)

        and: 'the event is received after the buffer time window passes'
        sleep(serverSentEventsService.bufferTimeout.toMillis() + 100) // --> Sleep the time window and add a prudential time to make sure the data has been received
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.userId == [trace.userId]
        subscriber.events.first()[0].data.workflowId == [trace.workflowId]
    }

    void "publish as many elements as the buffer size windows, the elements are received"() {
        given: 'several traces to publish'
        List<TraceSseResponse> traces = (1..serverSentEventsService.bufferCount).collect {
            TraceSseResponse.of(it, it.toString(), WorkflowAction.WORKFLOW_UPDATE)
        }

        and: 'more traces overflowing the buffer'
        Integer overflow = serverSentEventsService.bufferCount.intdiv(2)
        (1..overflow).each {
            TraceSseResponse.of(it, it.toString(), WorkflowAction.WORKFLOW_UPDATE)
        }

        and: 'subscribe to the events stream'
        TestSubscriber subscriber = serverSentEventsService.eventsFlowable.test()

        when: 'publish all the traces'
        traces.each {
            serverSentEventsService.publishEvent(it)
        }

        then: 'the data is received right away and the buffer contains just as many elements as the size window'
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.userId == (1..serverSentEventsService.bufferCount)
        subscriber.events.first()[0].data.workflowId == (1..serverSentEventsService.bufferCount)*.toString()
    }

}

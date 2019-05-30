package io.seqera.watchtower.service


import io.micronaut.test.annotation.MicronautTest
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import io.seqera.watchtower.Application
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException
import io.seqera.watchtower.pogo.exchange.live.LiveWorkflowUpdateMultiResponse
import io.seqera.watchtower.util.AbstractContainerBaseTest

import javax.inject.Inject
import java.time.Duration
import java.util.concurrent.TimeUnit

@MicronautTest(application = Application.class)
class ServerSentEventsServiceTest extends AbstractContainerBaseTest {

    @Inject
    ServerSentEventsServiceImpl serverSentEventsService


    void "create a flowable and retrieve it"() {
        given: 'an id for the flowable'
        Long id = 1

        when: 'create the flowable given a workflowId'
        serverSentEventsService.createFlowable(id)

        then: 'the flowable can be retrieved'
        serverSentEventsService.getFlowable(id)
    }

    void "create a flowable and publish some data for it"() {
        given: 'an id for the flowable'
        Long id = 2

        and: 'create the flowable'
        serverSentEventsService.createFlowable(id)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = new TestSubscriber()
        serverSentEventsService.getFlowable(id).subscribe(subscriber)

        when: 'publish some data for it'
        Map data = [text: 'Data published']
        serverSentEventsService.publishData(id, data)

        then: 'the subscriber has obtained the data correctly'
        subscriber.assertValueCount(1)
        subscriber.getEvents().first().first().data.text == 'Data published'
    }

    void "create a flowable and complete it"() {
        given: 'an id for the flowable'
        Long id = 3

        and: 'create the flowable'
        serverSentEventsService.createFlowable(id)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = new TestSubscriber()
        serverSentEventsService.getFlowable(id).subscribe(subscriber)

        when: 'complete the flowable'
        serverSentEventsService.completeFlowable(id)

        then: 'the flowable has been completed'
        subscriber.assertComplete()

        when: 'try to get the flowable again'
        serverSentEventsService.getFlowable(id)

        then: 'the flowable is no longer present'
        thrown(NonExistingFlowableException)
    }

    void "create a flowable and leave it idle until the timeout strikes"() {
        given: 'an id for the flowable'
        Long id = 4

        and: 'modify the time duration for this test'
        Duration previousDuration = serverSentEventsService.idleFlowableTimeout
        Duration shortDuration = Duration.ofMillis(300)
        serverSentEventsService.idleFlowableTimeout = shortDuration

        and: 'create the flowable'
        serverSentEventsService.createFlowable(id)

        and: 'subscribe to the flowable in order to retrieve data'
        TestSubscriber subscriber = new TestSubscriber()
        serverSentEventsService.getFlowable(id).subscribe(subscriber)

        when: 'sleep until the timeout is reached plus a prudential time'
        sleep(shortDuration.toMillis() + 100)

        then: 'the flowable has been completed'
        subscriber.assertComplete()

        cleanup: 'restore the timeout duration'
        serverSentEventsService.idleFlowableTimeout = previousDuration
    }

}

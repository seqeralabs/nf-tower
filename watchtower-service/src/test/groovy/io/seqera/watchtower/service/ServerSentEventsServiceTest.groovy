package io.seqera.watchtower.service

import io.micronaut.http.sse.Event
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.subscribers.TestSubscriber
import io.seqera.watchtower.Application
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException
import io.seqera.watchtower.util.AbstractContainerBaseTest

import javax.inject.Inject
import java.time.Duration

@MicronautTest(application = Application.class)
class ServerSentEventsServiceTest extends AbstractContainerBaseTest {

    @Inject
    ServerSentEventsServiceImpl serverSentEventsService


    void "create a flowable and retrieve it"() {
        given: 'a key for the flowable'
        String key = '1'

        when: 'create the flowable given a key'
        serverSentEventsService.createFlowable(key)

        then: 'the flowable can be retrieved'
        serverSentEventsService.getFlowable(key)
    }

    void "create a flowable and publish some data for it"() {
        given: 'a key for the flowable'
        String key = '2'

        and: 'create the flowable'
        serverSentEventsService.createFlowable(key)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = new TestSubscriber()
        serverSentEventsService.getFlowable(key).subscribe(subscriber)

        when: 'publish some data for it'
        Event event = Event.of([text: 'Data published'])
        serverSentEventsService.publishEvent(key, event)

        then: 'the subscriber has obtained the data correctly'
        subscriber.assertValueCount(1)
        subscriber.events.first()[0].data.text == 'Data published'
    }

    void "create a flowable and complete it"() {
        given: 'a key for the flowable'
        String key = '3'

        and: 'create the flowable'
        serverSentEventsService.createFlowable(key)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = new TestSubscriber()
        serverSentEventsService.getFlowable(key).subscribe(subscriber)

        when: 'complete the flowable'
        serverSentEventsService.completeFlowable(key)

        then: 'the flowable has been completed'
        subscriber.assertComplete()

        when: 'try to get the flowable again'
        serverSentEventsService.getFlowable(key)

        then: 'the flowable is no longer present'
        thrown(NonExistingFlowableException)
    }

    void "create a flowable and leave it idle until the timeout strikes"() {
        given: 'a key for the flowable'
        String key = '4'

        and: 'modify the time duration for this test'
        Duration previousDuration = serverSentEventsService.idleFlowableTimeout
        Duration shortDuration = Duration.ofMillis(300)
        serverSentEventsService.idleFlowableTimeout = shortDuration

        and: 'create the flowable'
        serverSentEventsService.createFlowable(key)

        and: 'subscribe to the flowable in order to retrieve data'
        TestSubscriber subscriber = new TestSubscriber()
        serverSentEventsService.getFlowable(key).subscribe(subscriber)

        when: 'sleep until the timeout plus a prudential time to make sure it was reached'
        sleep(shortDuration.toMillis() + 100)

        then: 'the flowable has been completed'
        subscriber.assertComplete()

        cleanup: 'restore the timeout duration'
        serverSentEventsService.idleFlowableTimeout = previousDuration
    }

}

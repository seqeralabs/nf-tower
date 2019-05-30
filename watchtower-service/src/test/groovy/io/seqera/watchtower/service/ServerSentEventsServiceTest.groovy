package io.seqera.watchtower.service


import io.micronaut.test.annotation.MicronautTest
import io.reactivex.subscribers.TestSubscriber
import io.seqera.watchtower.Application
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException
import io.seqera.watchtower.pogo.exchange.live.LiveWorkflowUpdateMultiResponse
import io.seqera.watchtower.util.AbstractContainerBaseTest

import javax.inject.Inject

@MicronautTest(application = Application.class)
class ServerSentEventsServiceTest extends AbstractContainerBaseTest {

    @Inject
    ServerSentEventsService liveWorkflowUpdateSseService


    void "create a flowable and retrieve it"() {
        given: 'an id for the flowable'
        Long id = 1

        when: 'create a flowable given a workflowId'
        liveWorkflowUpdateSseService.createFlowable(id)

        then: 'the flowable can be retrieved'
        liveWorkflowUpdateSseService.getFlowable(id)
    }

    void "create a flowable and publish some data for it"() {
        given: 'an id for the flowable'
        Long id = 2

        and: 'create a flowable'
        liveWorkflowUpdateSseService.createFlowable(id)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = new TestSubscriber()
        liveWorkflowUpdateSseService.getFlowable(id).subscribe(subscriber)

        when: 'publish some data for it'
        LiveWorkflowUpdateMultiResponse data = new LiveWorkflowUpdateMultiResponse(error: 'wathever')
        liveWorkflowUpdateSseService.publishUpdate(id, data)

        then: 'the subscriber has obtained the data correctly'
        subscriber.assertValueCount(1)
        subscriber.getEvents().first().first().data.error == 'wathever'
    }

    void "create a flowable and complete it"() {
        given: 'an id for the flowable'
        Long id = 3

        and: 'create a flowable'
        liveWorkflowUpdateSseService.createFlowable(id)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = new TestSubscriber()
        liveWorkflowUpdateSseService.getFlowable(id).subscribe(subscriber)

        when: 'complete the flowable'
        liveWorkflowUpdateSseService.completeFlowable(id)

        then: 'the flowable has been completed'
        subscriber.assertComplete()

        when: 'try to get the flowable again'
        liveWorkflowUpdateSseService.getFlowable(id)

        then: 'the flowable is no longer present'
        thrown(NonExistingFlowableException)
    }

}

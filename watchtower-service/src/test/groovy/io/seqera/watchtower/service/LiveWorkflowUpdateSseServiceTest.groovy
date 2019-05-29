package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.subscribers.TestSubscriber
import io.seqera.watchtower.Application
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException
import io.seqera.watchtower.pogo.exchange.live.LiveWorkflowUpdateMultiResponse
import io.seqera.watchtower.util.AbstractContainerBaseTest

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class LiveWorkflowUpdateSseServiceTest extends AbstractContainerBaseTest {

    @Inject
    LiveWorkflowUpdateSseService liveWorkflowUpdateSseService


    void "create a flowable and retrieve it"() {
        given: 'a workflow id'
        Long workflowId = 1

        when: 'create a flowable given a workflowId'
        liveWorkflowUpdateSseService.createFlowable(workflowId)

        then: 'the flowable can be retrieved'
        liveWorkflowUpdateSseService.getFlowable(workflowId)
    }

    void "create a flowable and publish some data for it"() {
        given: 'a workflow id'
        Long workflowId = 2

        and: 'create a flowable'
        liveWorkflowUpdateSseService.createFlowable(workflowId)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = new TestSubscriber()
        liveWorkflowUpdateSseService.getFlowable(workflowId).subscribe(subscriber)

        when: 'publish some data for it'
        LiveWorkflowUpdateMultiResponse data = new LiveWorkflowUpdateMultiResponse(error: 'wathever')
        liveWorkflowUpdateSseService.publishUpdate(workflowId, data)

        then: 'the subscriber has obtained the data correctly'
        subscriber.assertValueCount(1)
        subscriber.getEvents().first().first().data.error == 'wathever'
    }

    void "create a flowable and complete it"() {
        given: 'a workflow id'
        Long workflowId = 3

        and: 'create a flowable'
        liveWorkflowUpdateSseService.createFlowable(workflowId)

        and: 'subscribe to the flowable in order to retrieve the data'
        TestSubscriber subscriber = new TestSubscriber()
        liveWorkflowUpdateSseService.getFlowable(workflowId).subscribe(subscriber)

        when: 'complete the flowable'
        liveWorkflowUpdateSseService.completeFlowable(workflowId)

        then: 'the flowable has been completed'
        subscriber.assertComplete()

        and: 'the flowable is no longer present'
        liveWorkflowUpdateSseService.getFlowable(workflowId)
        thrown(NonExistingFlowableException)
    }

}

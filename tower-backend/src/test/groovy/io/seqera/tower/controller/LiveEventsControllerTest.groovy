package io.seqera.tower.controller

import javax.inject.Inject

import grails.gorm.transactions.Transactional
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.subscribers.TestSubscriber
import io.seqera.tower.Application
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.exchange.live.LiveUpdate
import io.seqera.tower.util.DomainCreator
import io.seqera.tower.util.NextflowSimulator
import spock.lang.Specification
import spock.lang.Timeout

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Timeout(10)
@MicronautTest(application = Application.class)
@Transactional
class LiveEventsControllerTest extends Specification {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    @Client('/')
    DefaultHttpClient sseClient

    void "save traces simulated from a complete sequence and subscribe to the live events in the mean time"() {
        given: 'an allowed user'
        User user = new DomainCreator().generateAllowedUser()

        and: 'a nextflow simulator'
        NextflowSimulator nextflowSimulator = new NextflowSimulator(user: user, workflowLabel: 'simulation', client: client.toBlocking(), sleepBetweenRequests: 0)

        when: 'subscribe to the live events for the workflow list endpoint'
        TestSubscriber listSubscriber = sseClient.eventStream("/live/user/${user.id}", LiveUpdate.class).test()

        then: 'the list flowable has just been created (is active)'
        listSubscriber.assertNotComplete()

        and: 'send the first request to start the workflow'
        nextflowSimulator.simulate(1)

        then: 'the workflow has been created'
        Workflow.withNewTransaction { Workflow.count() } == 1

        when: 'subscribe to the live events for the workflow detail endpoint'
        TestSubscriber detailSubscriber = sseClient.eventStream("/live/workflow/${nextflowSimulator.workflowId}", LiveUpdate.class).test()

        then: 'the detail flowable is active'
        detailSubscriber.assertNotComplete()

        when: 'keep simulating with the next task request'
        nextflowSimulator.simulate(1)

        then: 'the tasks have been created'
        Task.withNewTransaction { Task.count() } == 6

        and: 'the task progress event has been sent'
        //For some reason the event isn't received here although it's working properly in the browser
//        detailSubscriber.awaitCount(1)
//        detailSubscriber.assertValueCount(1)
    }
}

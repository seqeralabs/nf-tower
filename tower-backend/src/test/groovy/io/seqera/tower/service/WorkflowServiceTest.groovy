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

import spock.lang.Unroll

import javax.inject.Inject
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.exceptions.NonExistingWorkflowException
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import io.seqera.tower.util.TracesJsonBank
import io.seqera.tower.util.WorkflowTraceSnapshotStatus

@MicronautTest(application = Application.class)
@Transactional
class WorkflowServiceTest extends AbstractContainerBaseTest {

    @Inject
    WorkflowService workflowService


    void "start a workflow given a started trace"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflow
        Workflow.withNewTransaction {
            workflow = workflowService.processTraceWorkflowRequest(workflowStartedTraceJson, owner)
        }

        then: "the workflow has been correctly saved"
        workflow.id
        workflow.owner
        workflow.checkIsStarted()
        workflow.submit
        !workflow.complete
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }
    }

    void "start a workflow given a started trace, then complete the workflow given a succeeded trace"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processTraceWorkflowRequest(workflowStartedTraceJson, owner)
        }

        then: "the workflow has been correctly saved"
        workflowStarted.id
        workflowStarted.owner
        workflowStarted.checkIsStarted()
        workflowStarted.submit
        !workflowStarted.complete

        when: "given a workflow succeeded trace, unmarshall the succeeded JSON to a workflow"
        TraceWorkflowRequest workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', workflowStarted.id, WorkflowTraceSnapshotStatus.SUCCEEDED)
        Workflow workflowSucceeded
        Workflow.withNewTransaction {
            workflowSucceeded = workflowService.processTraceWorkflowRequest(workflowSucceededTraceJson, owner)
        }

        then: "the workflow has been completed"
        workflowSucceeded.id == workflowStarted.id
        workflowSucceeded.checkIsSucceeded()
        workflowSucceeded.submit
        workflowSucceeded.complete
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }

        and: "there is a metrics info"
        def metrics = workflowService.findMetrics(workflowSucceeded)
        metrics.size()==1
        metrics.first().process == 'sayHello'
        metrics.first().cpu
        metrics.first().time
        metrics.first().reads
        metrics.first().writes
        metrics.first().cpuUsage

    }

    void "start a workflow given a started trace, then complete the workflow given a failed trace"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processTraceWorkflowRequest(workflowStartedTraceJson, owner)
        }

        then: "the workflow has been correctly saved"
        workflowStarted.id
        workflowStarted.owner
        workflowStarted.checkIsStarted()
        workflowStarted.submit
        !workflowStarted.complete
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }

        when: "given a workflow failed trace, unmarshall the failed JSON to a workflow"
        TraceWorkflowRequest workflowFailedTraceJson = TracesJsonBank.extractWorkflowJsonTrace('failed', workflowStarted.id, WorkflowTraceSnapshotStatus.FAILED)
        Workflow workflowFailed
        Workflow.withNewTransaction {
            workflowFailed = workflowService.processTraceWorkflowRequest(workflowFailedTraceJson, owner)
        }

        then: "the workflow has been completed"
        workflowFailed.id == workflowStarted.id
        workflowFailed.checkIsFailed()
        workflowFailed.submit
        workflowFailed.complete
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }

        and: "there is a metrics info"
        def metrics = workflowService.findMetrics(workflowFailed)
        metrics.size()==1
        metrics.first().process == 'sayHello'
        metrics.first().cpu
        metrics.first().time
        metrics.first().reads
        metrics.first().writes
        metrics.first().cpuUsage


    }

    void "start a workflow given a started trace, then try to start the same one"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStarted1TraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted1
        Workflow.withNewTransaction {
            workflowStarted1 = workflowService.processTraceWorkflowRequest(workflowStarted1TraceJson, owner)
        }

        then: "the workflow has been correctly saved"
        workflowStarted1.id
        workflowStarted1.owner
        workflowStarted1.checkIsStarted()
        workflowStarted1.submit
        !workflowStarted1.complete
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }

        when: "given a workflow started trace with the same workflowId, unmarshall the started JSON to a second workflow"
        TraceWorkflowRequest workflowStarted2TraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', workflowStarted1.id, WorkflowTraceSnapshotStatus.STARTED)
        Workflow workflowStarted2
        Workflow.withNewTransaction {
            workflowStarted2 = workflowService.processTraceWorkflowRequest(workflowStarted2TraceJson, owner)
        }

        then: "the second workflow is treated as a new one, and sessionId/runName combination cannot be repeated"
        workflowStarted2.errors.getFieldError('runName').code == 'unique'
        Workflow.withNewTransaction {
            Workflow.count() == 1
        }
    }

    void "try to start a workflow given a started trace without sessionId"() {
        given: "a workflow JSON started trace without sessionId"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)
        workflowStartedTraceJson.workflow.sessionId = null

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted
        Workflow.withNewTransaction {
            workflowStarted = workflowService.processTraceWorkflowRequest(workflowStartedTraceJson, owner)
        }

        then: "the workflow has validation errors"
        workflowStarted.hasErrors()
        workflowStarted.errors.getFieldError('sessionId').code == 'nullable'
        Workflow.withNewTransaction {
            Workflow.count() == 0
        }
    }

    void "try to complete a workflow given a succeeded trace for a non existing workflow"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', 123, WorkflowTraceSnapshotStatus.SUCCEEDED)

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflowSucceeded
        Workflow.withNewTransaction {
            workflowSucceeded = workflowService.processTraceWorkflowRequest(workflowSucceededTraceJson, owner)
        }

        then: "the workflow has been correctly saved"
        thrown(NonExistingWorkflowException)
    }

    @Unroll
    void "list some workflows belonging to user"() {
        given: 'the owner of the workflows'
        DomainCreator creator = new DomainCreator()
        User owner = creator.createUser()

        and: 'some workflows of the owner with increasing start dates and custom project and run name'
        List<Workflow> userWorkflows = []
        ZonedDateTime now = ZonedDateTime.now()
        nUserWorkflows.times {
            userWorkflows << creator.createWorkflow(owner: owner, start: now.plusSeconds(it).toOffsetDateTime(), projectName: "project${it}", runName: "runName${it}")
        }

        and: 'some other workflows'
        List<Workflow> otherWorkflows = []
        nOtherWorkflows.times {
            otherWorkflows << creator.createWorkflow()
        }

        when: 'list the workflows of the user'
        List<Workflow> obtainedUserWorkflows = workflowService.listByOwner(owner, max, offset, null)

        and: 'compute the number of expected workflows'
        Integer nExpectedWorkflows = (max == null || nUserWorkflows < max) ? nUserWorkflows : max
        Integer rangeOrigin = offset ?: 0

        then: 'the obtained workflows are as expected'
        obtainedUserWorkflows.size() == nExpectedWorkflows
        obtainedUserWorkflows.id == userWorkflows.sort { w1, w2 -> w2.start <=> w1.start }[rangeOrigin..<(rangeOrigin + nExpectedWorkflows)].id

        where: 'the pagination params are'
        nUserWorkflows | nOtherWorkflows | max  | offset
        20             | 10              | 10   | 0
        20             | 10              | 10   | 10
        20             | 10              | 10   | 0
        20             | 10              | 10   | 10
        20             | 10              | null | null
    }

    @Unroll
    void "search workflows belonging to a user by text"() {
        given: 'the owner of the workflows'
        DomainCreator creator = new DomainCreator()
        User owner = creator.createUser()

        and: 'some workflows of the owner with custom project and run name'
        List<Workflow> userWorkflows = []
        4.times {
            userWorkflows << creator.createWorkflow(owner: owner, projectName: "project${it}", runName: "runName${it}", commitId: "commitId${it}")
        }

        when: 'search for the workflows associated with the user'
        List<Workflow> obtainedWorkflows = workflowService.listByOwner(owner, 10, 0, search)

        then: 'the obtained workflows are as expected'
        obtainedWorkflows.sort { it.commitId }.commitId == expectedWorkflowCommitIds

        where: 'the search params are'
        search      | expectedWorkflowCommitIds
        'project%'  | ["commitId0", "commitId1", "commitId2", "commitId3"]
        'runName%'  | ["commitId0", "commitId1", "commitId2", "commitId3"]
        'PrOjEct%'  | ["commitId0", "commitId1", "commitId2", "commitId3"]
        'rUnNAme%'  | ["commitId0", "commitId1", "commitId2", "commitId3"]
        'project0'  | ["commitId0"]
        'runName1'  | ["commitId1"]
        '%a%'       | ["commitId0", "commitId1", "commitId2", "commitId3"]
    }

    void 'delete a workflow'() {
        given: 'a workflow with some metrics entries'
        DomainCreator domainCreator = new DomainCreator()
        Workflow workflow = domainCreator.createWorkflow()
        domainCreator.createWorkflowMetrics(workflow)
        domainCreator.createWorkflowMetrics(workflow)

        and: 'some tasks associated with the workflow'
        (1..3).each {
            domainCreator.createTask(taskId: it, workflow: workflow)
        }

        when: 'delete the workflow'
        Workflow.withNewTransaction {
            workflowService.delete(workflow.refresh())
        }

        then: 'the workflow is no longer in the database'
        Workflow.withNewTransaction {
            Workflow.count() == 0
        }
    }

    def 'should find comments' () {
        def creator = new DomainCreator(validate: false)
        def user = creator.createUser()
        def workflow = creator.createWorkflow()

        def t0 = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        def t1 = t0.minusMinutes(10)
        def t2 = t0
        WorkflowComment.withNewTransaction {
            new WorkflowComment(author: user,
                    text: 'First hello',
                    workflow: workflow,
                    lastUpdated: t1,
                    dateCreated: t1)
                    .save(failOnError:true)

            new WorkflowComment(author: user,
                    text: 'Second hello',
                    workflow: workflow,
                    lastUpdated: t2,
                    dateCreated: t2)
                    .save(failOnError:true)
        }

        when:
        def comments = workflowService.getComments(workflow)
        then:
        comments.size() == 2
        comments[0].dateCreated == t2
        comments[0].text == 'Second hello'
        comments[1].dateCreated == t1
        comments[1].text == 'First hello'
    }

}

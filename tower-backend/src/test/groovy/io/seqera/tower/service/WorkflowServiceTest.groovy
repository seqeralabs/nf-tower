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
import javax.validation.ValidationException
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.TaskData
import io.seqera.tower.domain.User
import io.seqera.tower.domain.WfManifest
import io.seqera.tower.domain.WfNextflow
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.domain.WorkflowProcess
import io.seqera.tower.enums.WorkflowStatus
import io.seqera.tower.exceptions.NonExistingWorkflowException
import io.seqera.tower.exchange.trace.TraceWorkflowRequest
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import io.seqera.tower.util.DomainHelper
import io.seqera.tower.util.TracesJsonBank
import io.seqera.tower.util.WorkflowTraceSnapshotStatus
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Unroll

@MicronautTest(application = Application.class)
@Transactional
class WorkflowServiceTest extends AbstractContainerBaseTest {

    @Inject
    WorkflowService workflowService

    @Inject
    TransactionService tx

    void "start a workflow given a started trace"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflow
        Workflow.withNewTransaction {
            workflowStartedTraceJson.workflow.id = '123'
            workflow = workflowService.processTraceWorkflowRequest(workflowStartedTraceJson, owner)
        }

        then: "the workflow has been correctly saved"
        workflow.id == '123'
        workflow.owner
        workflow.checkIsRunning()
        workflow.@status == WorkflowStatus.RUNNING
        workflow.status == WorkflowStatus.RUNNING
        workflow.submit
        !workflow.complete
        Workflow.withNewTransaction { Workflow.count() } == 1
        WorkflowProcess.withNewTransaction { WorkflowProcess.count() } == 3
    }

    void "start a workflow given a started trace, then complete the workflow given a succeeded trace"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowStartedTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', null, WorkflowTraceSnapshotStatus.STARTED)

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted = Workflow.withNewTransaction {
            workflowService.processTraceWorkflowRequest(workflowStartedTraceJson, owner)
        }

        then: "the workflow has been correctly saved"
        workflowStarted.id
        workflowStarted.owner
        workflowStarted.checkIsRunning()
        workflowStarted.@status == WorkflowStatus.RUNNING
        workflowStarted.status == WorkflowStatus.RUNNING
        workflowStarted.submit
        !workflowStarted.complete

        when: "given a workflow succeeded trace, unmarshall the succeeded JSON to a workflow"
        TraceWorkflowRequest workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', workflowStarted.id, WorkflowTraceSnapshotStatus.SUCCEEDED)
        Workflow workflowSucceeded = Workflow.withNewTransaction {
            workflowService.processTraceWorkflowRequest(workflowSucceededTraceJson, owner)
        }

        then: "the workflow has been completed"
        workflowSucceeded.id == workflowStarted.id
        workflowSucceeded.checkIsSucceeded()
        workflowSucceeded.@status == WorkflowStatus.SUCCEEDED
        workflowSucceeded.status == WorkflowStatus.SUCCEEDED
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
        workflowStarted.checkIsRunning()
        workflowStarted.@status == WorkflowStatus.RUNNING
        workflowStarted.status == WorkflowStatus.RUNNING
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
        workflowFailed.status == WorkflowStatus.FAILED
        workflowFailed.status == WorkflowStatus.FAILED
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
        def WORKFLOW_ID = 'ID-100'
        TraceWorkflowRequest workflowStarted1TraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', WORKFLOW_ID, WorkflowTraceSnapshotStatus.STARTED)

        and: 'a user owner for the workflow'
        User owner = new DomainCreator().createUser()

        when: "unmarshall the JSON to a workflow"
        Workflow workflowStarted1 = Workflow.withNewSession { workflowService.processTraceWorkflowRequest(workflowStarted1TraceJson, owner) }

        then: "the workflow has been correctly saved"
        workflowStarted1.id == WORKFLOW_ID
        workflowStarted1.owner
        workflowStarted1.checkIsRunning()
        workflowStarted1.@status == WorkflowStatus.RUNNING
        workflowStarted1.status == WorkflowStatus.RUNNING
        workflowStarted1.submit
        !workflowStarted1.complete
        Workflow.withNewTransaction { Workflow.count() } == 1

        when: "given a workflow started trace with the same workflowId, unmarshall the started JSON to a second workflow"
        
        TraceWorkflowRequest workflowStarted2TraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', WORKFLOW_ID, WorkflowTraceSnapshotStatus.STARTED)
        Workflow workflowStarted2 = Workflow.withNewSession {
            workflowService.processTraceWorkflowRequest(workflowStarted2TraceJson, owner)
        }

        then: "the second workflow is treated as a new one, and sessionId/runName combination cannot be repeated"
        thrown(DataIntegrityViolationException)
        Workflow.withNewTransaction { Workflow.count() } == 1 
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
        thrown(ValidationException)

        Workflow.withNewTransaction {
            Workflow.count() == 0
        }
    }

    void "try to complete a workflow given a succeeded trace for a non existing workflow"() {
        given: "a workflow JSON started trace"
        TraceWorkflowRequest workflowSucceededTraceJson = TracesJsonBank.extractWorkflowJsonTrace('success', '123', WorkflowTraceSnapshotStatus.SUCCEEDED)

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
        def now = OffsetDateTime.now()
        nUserWorkflows.times {
            userWorkflows << creator.createWorkflow(owner: owner, start: now.plusSeconds(it), projectName: "project${it}", runName: "runName${it}")
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
        def creator = new DomainCreator()
        def workflow = creator.createWorkflow()
        creator.createWorkflowMetrics(workflow)
        and:
        creator.createProcess(workflow: workflow, position: 0, name: 'foo')
        and:
        creator.createProcessLoad(workflow: workflow, process: 'foo')
        creator.createWorkflowLoad(workflow: workflow)

        and: 'some tasks associated with the workflow'
        (1..3).each {
            creator.createTask(taskId: it, workflow: workflow)
        }

        when: 'delete the workflow'
        Workflow.withNewTransaction { workflowService.delete(workflow.refresh()) }

        then: 'the workflow is no longer in the database'
        Workflow.withNewTransaction { Workflow.count() } == 0
    }

    def 'delete workflow keep task data records' () {
        given:
        def creator = new DomainCreator()
        def w1 = creator.createWorkflow(sessionId: 'abc', runName: 'alpha')
        def w2 = creator.createWorkflow(sessionId: 'zzz', runName: 'delta')
        def w3 = creator.createWorkflow(sessionId: 'zzz', runName: 'omega')
        and:
        // w1 has 2 tasks
        def t1= creator.createTask(workflow: w1)
        def t2= creator.createTask(workflow: w1)

        // w2 has 3 tasks
        def p1= creator.createTask(workflow: w2)
        def p2=creator.createTask(workflow: w2)
        def p3=creator.createTask(workflow: w2)

        // w3 has 3 tasks, 2 of them are cached from the previous run
        def q1 = creator.createTask(workflow: w3, data: p1.data)
        def q2 = creator.createTask(workflow: w3, data: p2.data)
        def q3 = creator.createTask(workflow: w3)

        when:
        tx.withNewTransaction { workflowService.delete(w1) }
        then:
        tx.withNewTransaction { Task.countByWorkflow(w1) } == 0
        tx.withNewTransaction { TaskData.countBySessionId(w1.sessionId) } ==0
        and:
        tx.withNewTransaction { Task.countByWorkflow(w2) } == 3
        tx.withNewTransaction { TaskData.countBySessionId(w2.sessionId) } ==4
        and:
        tx.withNewTransaction { Task.countByWorkflow(w2) } == 3
        tx.withNewTransaction { TaskData.countBySessionId(w3.sessionId) } ==4


        when:
        tx.withNewTransaction { workflowService.delete(w2) }
        then:
        tx.withNewTransaction { Task.countByWorkflow(w2) } == 0
        tx.withNewTransaction { TaskData.countBySessionId(w2.sessionId) } ==3
        and:
        tx.withNewTransaction { Task.countByWorkflow(w3) } == 3
        tx.withNewTransaction { TaskData.countBySessionId(w3.sessionId) } ==3


        when:
        tx.withNewTransaction { workflowService.delete(w3) }
        then:
        tx.withNewTransaction { Task.countByWorkflow(w3) } == 0
        tx.withNewTransaction { TaskData.countBySessionId(w3.sessionId) } ==0
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


    def 'workflow key should be case sensitive' () {
        given:
        def creator = new DomainCreator()
        User user = creator.createUser()
        def w1 = creator.createWorkflow(owner: user, id: 'abc')
        def w2 = creator.createWorkflow(owner: user, id: 'ABC')

        when:
        w1 = tx.withTransaction { w1.save() }
        then:
        tx.withTransaction { Workflow.get(w1.id) }

        when:
        w2 = tx.withTransaction { w2.save() }
        then:
        tx.withTransaction { Workflow.get(w2.id) }

        and:
        tx.withTransaction { Workflow.count() } ==2

        when:
        def w3 = creator.createWorkflow(owner: user, id: 'ABC')
        tx.withTransaction { w3.save() }
        then:
        thrown(DataIntegrityViolationException)
    }

    def 'should save versions' () {
        given:
        def creator = new DomainCreator()
        def user = creator.createUser()
        def meta = new WfNextflow(version_: '20.01.1', build: '12345')
        def manifest = new WfManifest(version_: '1.2.3', nextflowVersion: '18.0.0', author: 'Mr foo')

        when:
        def w1 = creator.createWorkflow(owner: user, nextflow: meta, manifest: manifest)
        then:
        w1.version == 0
        w1.nextflow.version_ == '20.01.1'
        w1.nextflow.build == '12345'
        w1.manifest.version_ == '1.2.3'
        w1.manifest.nextflowVersion == '18.0.0'
        w1.manifest.author == 'Mr foo'

        when:
        def w2 = Workflow.get(w1.id)
        then:
        w2.version == 0
        w2.nextflow.version_ == '20.01.1'
        w2.nextflow.build == '12345'
        w2.manifest.version_ == '1.2.3'
        w2.manifest.nextflowVersion == '18.0.0'
        w2.manifest.author == 'Mr foo'

    }


    def 'should map version to json object' () {

        given:
        def nextflow = new WfNextflow(version_: '20.01.1', build: '12345')
        def manifest = new WfManifest(version_: '1.2.3', nextflowVersion: '18.0.0', author: 'Mr foo')
        def workflow = new Workflow( nextflow: nextflow, manifest: manifest )

        when:
        def json = DomainHelper.toJson(workflow)
        def map = new JsonSlurper().parseText(json) as Map
        then:
        map.nextflow.version == '20.01.1'
        map.nextflow.build == '12345'
        map.manifest.version == '1.2.3'
        map.manifest.nextflowVersion == '18.0.0'

    }

    def 'should parse version strings' () {

        given:
        def REQ = '''
            {
                "id": "abc",
                "manifest": {
                    "nextflowVersion": "18.0.0",
                    "defaultBranch": "master",
                    "version": "3.3.3",
                    "gitmodules": true,
                    "mainScript": "main.nf",
                    "author": "Mr Bar"
                },
                "nextflow": {
                  "build": "54321",
                  "version": "19.01.01"
                }
            }
            '''.stripIndent()

        when:
        def wf = DomainHelper.mapper.readValue(REQ, Workflow)
        then:
        wf.id == "abc"
        and:
        wf.manifest.version_ == "3.3.3"
        wf.manifest.nextflowVersion == "18.0.0"
        wf.manifest.gitmodules == 'true'
        wf.manifest.mainScript == 'main.nf'
        wf.manifest.defaultBranch == 'master'
        and:
        wf.nextflow.version_ == "19.01.01"
        wf.nextflow.build == "54321"

    }

    def 'should deserialize list of modules' () {
        when:
        def REQ1 = '''
        {
            "defaultBranch": "master",
            "version": "3.3.3",
            "gitmodules": ["foo","bar"],
            "mainScript": "main.nf",
            "author": "Mr Bar"
        }
        '''
        def manifest1 = DomainHelper.mapper.readValue(REQ1, WfManifest)
        then:
        manifest1.gitmodules == 'foo,bar'

        when:
        def REQ2 = '''
        {
            "defaultBranch": "other",
            "gitmodules": "long_module_name",
            "mainScript": "main.nf",
            "author": "Mr Bar"
        }
        '''
        def manifest2 = DomainHelper.mapper.readValue(REQ2, WfManifest)
        then:
        manifest2.gitmodules == 'long_module_name'
    }

    def 'should get process names' () {

        given:
        def creator = new DomainCreator()
        def wf = creator.createWorkflow()
        def p1 = creator.createProcess(workflow: wf, name:'roger', position: 2)
        def p2 = creator.createProcess(workflow: wf, name:'bravo', position: 1)
        def p3 = creator.createProcess(workflow: wf, name:'alpha', position: 0)

        when:
        def names = workflowService.getProcessNames(wf)
        then:
        names == ['alpha','bravo','roger']
    }

}

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

package io.seqera.tower.service.progress

import javax.inject.Inject
import javax.inject.Singleton

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.ProcessLoad
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
@Transactional
class ProgressOperationsTest extends AbstractContainerBaseTest {

    static final _1GB = 1<<20
    static final _2GB = 2 * _1GB
    static final _3GB = 3 * _1GB
    static final _4GB = 4 * _1GB
    static final _5GB = 5 * _1GB
    static final _6GB = 6 * _1GB
    static final _7GB = 7 * _1GB

    @Inject
    ProgressOperationsImpl progressOp

    @Inject
    TransactionService tx

    @Inject
    ApplicationContext ctx

    @Singleton
    ProgressStore getStatsStore() {
        ctx.getBean(LocalStatsStore)
    }


    def 'should collect stats' () {
        given:
        def names = ['foo','bar']
        String wfId = '9ds90d'
        def list = [ new Task(id:1, process: 'foo', status: TaskStatus.RUNNING, cpus: 1, memory: _1GB) ]

        /*
         * one task is running
         */
        when:
        progressOp.create(wfId, names)
        progressOp.updateStats(wfId, list)
        def result = progressOp.computeStats(wfId)

        then:
        result.workflowProgress.running == 1
        result.workflowProgress.submitted == 0
        result.workflowProgress.pending == 0
        result.workflowProgress.succeeded == 0
        result.workflowProgress.failed == 0
        result.workflowProgress.cached == 0
        and:
        result.workflowProgress.loadTasks == 1
        result.workflowProgress.loadCpus == 1
        result.workflowProgress.loadMemory == _1GB
        and:
        result.workflowProgress.peakTasks == 1
        result.workflowProgress.peakCpus == 1
        result.workflowProgress.peakMemory == _1GB
        and:
        result.workflowProgress.running == 1
        result.workflowProgress.submitted == 0
        result.workflowProgress.succeeded == 0
        result.workflowProgress.failed == 0
        result.workflowProgress.cached == 0
        result.workflowProgress.pending == 0
        and:
        // total are expected to be updated only on completion
        result.workflowProgress.cpus == 0
        result.processesProgress.size() == 2
        and:
        with( result.processesProgress.get(0) ) {
            process == 'foo'
            running == 1
            submitted == 0
            pending == 0
            failed == 0
            succeeded == 0
            cached == 0
        }
        and:
        with( result.processesProgress.get(1) ) {
            process == 'bar'
            running == 0
            submitted == 0
            pending == 0
            failed == 0
            succeeded == 0
            cached == 0
        }

        /*
         *  add two more running tasks
         */
        when:
        progressOp.updateStats(wfId,
                    [ new Task(id:2, process: 'foo', status: TaskStatus.RUNNING, cpus: 2, memory: 2 * _1GB),
                      new Task(id:3, process: 'bar', status: TaskStatus.RUNNING, cpus: 3, memory: 3 * _1GB) ] )
        result = progressOp.computeStats(wfId)
        then:
        result.workflowProgress.loadTasks == 3
        result.workflowProgress.loadCpus == 6
        result.workflowProgress.loadMemory == 6 * _1GB
        and:
        result.workflowProgress.peakTasks == 3
        result.workflowProgress.peakCpus == 6
        result.workflowProgress.peakMemory == 6 * _1GB
        and:
        result.workflowProgress.cpus == 0
        result.processesProgress.size() == 2
        and:
        result.workflowProgress.running == 3
        result.workflowProgress.submitted == 0
        result.workflowProgress.pending == 0
        result.workflowProgress.succeeded == 0
        result.workflowProgress.failed == 0
        result.workflowProgress.cached == 0
        and:
        with( result.processesProgress.get(0) ) {
            process == 'foo'
            running == 2
            submitted == 0
            pending == 0
            failed == 0
            succeeded == 0
            cached == 0
        }
        and:
        with( result.processesProgress.get(1) ) {
            process == 'bar'
            running == 1
            submitted == 0
            pending == 0
            failed == 0
            succeeded == 0
            cached == 0
        }


        /*
         * complete task 1
         */
        when:
        progressOp.updateStats(wfId, [new Task(id:1, process: 'foo', status: TaskStatus.COMPLETED, cpus: 1, memory: _1GB, peakRss: _1GB, realtime: 100, pcpu: 100, rchar: 100, wchar: 200, volCtxt: 300, invCtxt: 400)] )
        result = progressOp.computeStats(wfId)
        // one task was removed from load
        then:
        result.workflowProgress.loadTasks == 2
        result.workflowProgress.loadCpus == 5
        result.workflowProgress.loadMemory == 5 * _1GB
        // peak remain the same 
        and:
        result.workflowProgress.peakTasks == 3
        result.workflowProgress.peakCpus == 6
        result.workflowProgress.peakMemory == 6 * _1GB
        and:
        result.workflowProgress.running == 2
        result.workflowProgress.succeeded == 1
        result.workflowProgress.submitted == 0
        result.workflowProgress.pending == 0
        result.workflowProgress.failed == 0
        result.workflowProgress.cached == 0

        and:
        with( result.processesProgress.get(0) ) {
            process == 'foo'
            running == 1
            succeeded == 1
            submitted == 0
            pending == 0
            failed == 0
            cached == 0
            //
            cpus == 1
            cpuTime == 100
            cpuLoad == 100
            memoryRss == _1GB
            memoryReq == _1GB
            readBytes == 100
            writeBytes == 200
            volCtxSwitch == 300
            invCtxSwitch == 400
        }
        and:
        with( result.processesProgress.get(1) ) {
            process == 'bar'
            running == 1
            submitted == 0
            pending == 0
            failed == 0
            succeeded == 0
            cached == 0
            //
            cpus == 0
        }

        /*
         * complete task 2
         */
        when:
        progressOp.updateStats(wfId, [new Task(id:2, process: 'foo', status: TaskStatus.FAILED, cpus: 2, memory: _2GB, peakRss: _2GB, realtime: 100, pcpu: 200, rchar: 100, wchar: 200, volCtxt: 300, invCtxt: 400)] )
        result = progressOp.computeStats(wfId)
        then:
        result.workflowProgress.loadTasks == 1
        result.workflowProgress.loadCpus == 3
        result.workflowProgress.loadMemory == 3 * _1GB
        // peak remain the same
        and:
        result.workflowProgress.peakTasks == 3
        result.workflowProgress.peakCpus == 6
        result.workflowProgress.peakMemory == 6 * _1GB
        and:
        result.workflowProgress.running == 1
        result.workflowProgress.succeeded == 1
        result.workflowProgress.failed == 1
        result.workflowProgress.submitted == 0
        result.workflowProgress.pending == 0
        result.workflowProgress.cached == 0

        and:
        with( result.processesProgress.get(0) ) {
            process == 'foo'
            running == 0
            succeeded == 1
            failed == 1
            submitted == 0
            pending == 0
            cached == 0
            //
            cpus == 3
            cpuTime == 300
            cpuLoad == 300
            memoryRss == _3GB
            memoryReq == _3GB
            readBytes == 200
            writeBytes == 400
            volCtxSwitch == 600
            invCtxSwitch == 800
        }
        and:
        with( result.processesProgress.get(1) ) {
            process == 'bar'
            running == 1
            submitted == 0
            pending == 0
            failed == 0
            succeeded == 0
            cached == 0
            //
            cpus == 0
        }


        /*
         * complete task 3
         */
        when:
        progressOp.updateStats(wfId, [new Task(id:3, process: 'bar', status: TaskStatus.COMPLETED, cpus: 3, memory: _3GB, peakRss: _2GB, realtime: 100, pcpu: 200, rchar: 100, wchar: 200, volCtxt: 300, invCtxt: 400)] )
        result = progressOp.computeStats(wfId)
        then:
        result.workflowProgress.loadTasks == 0
        result.workflowProgress.loadCpus == 0
        result.workflowProgress.loadMemory == 0
        and:
        result.workflowProgress.peakTasks == 3
        result.workflowProgress.peakCpus == 6
        result.workflowProgress.peakMemory == _6GB
        and:
        result.workflowProgress.running == 0
        result.workflowProgress.succeeded == 2
        result.workflowProgress.failed == 1
        result.workflowProgress.submitted == 0
        result.workflowProgress.pending == 0
        result.workflowProgress.cached == 0
        and:
        result.workflowProgress.cpus == 6
        result.workflowProgress.memoryReq == _6GB
        and:
        with( result.processesProgress.get(0) ) {
            process == 'foo'
            running == 0
            succeeded == 1
            failed == 1
            submitted == 0
            pending == 0
            cached == 0
        }
        and:
        with( result.processesProgress.get(1) ) {
            process == 'bar'
            running == 0
            succeeded == 1
            failed == 0
            submitted == 0
            pending == 0
            cached == 0
        }


        /*
         * complete task 4 cached
         */
        when:
        progressOp.updateStats(wfId, [new Task(id:4, process: 'bar', status: TaskStatus.CACHED, cpus: 4, memory: _4GB, peakRss: _2GB, realtime: 100, pcpu: 200, rchar: 100, wchar: 200, volCtxt: 300, invCtxt: 400)] )
        result = progressOp.computeStats(wfId)
        then:
        result.workflowProgress.loadTasks == 0
        result.workflowProgress.loadCpus == 0
        result.workflowProgress.loadMemory == 0
        and:
        result.workflowProgress.peakTasks == 3
        result.workflowProgress.peakCpus == 6
        result.workflowProgress.peakMemory == _6GB
        and:
        result.workflowProgress.running == 0
        result.workflowProgress.succeeded == 2
        result.workflowProgress.failed == 1
        result.workflowProgress.submitted == 0
        result.workflowProgress.pending == 0
        result.workflowProgress.cached == 1
        and:
        result.workflowProgress.cpus == 10
        result.workflowProgress.memoryReq == 10 * _1GB

        and:
        with( result.processesProgress.get(0) ) {
            process == 'foo'
            running == 0
            succeeded == 1
            failed == 1
            submitted == 0
            pending == 0
            cached == 0
        }
        and:
        with( result.processesProgress.get(1) ) {
            process == 'bar'
            running == 0
            succeeded == 1
            failed == 0
            submitted == 0
            pending == 0
            cached == 1
        }
    }

    def 'should load saved data' () {
        given:
        def creator = new DomainCreator()
        def wf1 = creator.createWorkflow()
        creator.createProcess(workflow: wf1, name:'roger', position: 2)
        creator.createProcess(workflow: wf1, name:'bravo', position: 1)
        creator.createProcess(workflow: wf1, name:'alpha', position: 0)
        and:
        creator.createProcessLoad(workflow: wf1, process:'roger', peakCpus: 10)
        creator.createProcessLoad(workflow: wf1, process:'bravo', peakCpus: 20)
        creator.createProcessLoad(workflow: wf1, process:'alpha', peakCpus: 30)
        and:
        creator.createWorkflowLoad(workflow: wf1, running: 2, succeeded: 10, failed: 1)

        and:
        def wf2 = creator.createWorkflow()
        creator.createProcess(workflow: wf2, name:'roger', position: 2)
        creator.createProcess(workflow: wf2, name:'bravo', position: 1)
        creator.createProcessLoad(workflow: wf2, process:'roger', peakCpus: 10)
        creator.createProcessLoad(workflow: wf2, process:'bravo', peakCpus: 20)

        when:
        def result = progressOp.load(wf1.id)
        then:
        result.workflowProgress.running ==2
        result.workflowProgress.succeeded ==10
        result.workflowProgress.failed ==1
        and:
        result.processesProgress.size() == 3
        and:
        result.processesProgress[0].process == 'alpha'
        result.processesProgress[0].peakCpus == 30
        and:
        result.processesProgress[1].process == 'bravo'
        result.processesProgress[1].peakCpus == 20
        and:
        result.processesProgress[2].process == 'roger'
        result.processesProgress[2].peakCpus == 10
    }

    def 'should return null load' () {
        expect:
        progressOp.load('unknow-xx') == null
    }
    
    def 'should persist progress' () {
        given:
        def workflow = new DomainCreator().createWorkflow()
        and:
        def names = ['foo','bar']
        def list = [
                new Task(id:1, process: 'foo', status: TaskStatus.COMPLETED, cpus: 1, memory: _1GB),
                new Task(id:2, process: 'foo', status: TaskStatus.COMPLETED, cpus: 2, memory: _2GB),
                new Task(id:3, process: 'bar', status: TaskStatus.FAILED,    cpus: 4, memory: _4GB) ]

        and:
        progressOp.create(workflow.id, names)
        progressOp.updateStats(workflow.id, list)
        progressOp.computeStats(workflow.id)

        when:
        tx.withNewTransaction { progressOp.complete(workflow.id) }
        then:
        with(WorkflowLoad.findByWorkflow(workflow)) {
            succeeded == 2
            failed == 1
            cpus == 7
            memoryReq == _7GB
        }

        and:
        with(ProcessLoad.findByWorkflowAndProcess(workflow, 'foo')) {
            succeeded == 2
            failed == 0
            cpus == 3
            memoryReq == _3GB
        }

        and:
        with(ProcessLoad.findByWorkflowAndProcess(workflow, 'bar')) {
            succeeded == 0
            failed == 1
            cpus == 4
            memoryReq == _4GB
        }
    }
}

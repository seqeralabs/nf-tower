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

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.service.LiveEventsService
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import io.seqera.tower.util.DomainHelper

@MicronautTest(application = Application.class)
@Transactional
class ProgressServiceDeprecatedTest extends AbstractContainerBaseTest {

    @Inject
    ProgressService progressService

    @Inject
    LiveEventsService liveEventsService

    @Inject TransactionService tx

    void "compute simple progress" () {
        given:
        def creator = new DomainCreator()

        String process1 = 'process1'
        Workflow wf = creator.createWorkflow()
        creator.createProcess(workflow: wf, name:process1, position:0)
        creator.createTask(
                workflow: wf,
                status: TaskStatus.COMPLETED,
                process: process1,
                cpus: 1,
                realtime: 2,
                peakRss: 1,
                memory: 2,
                rchar: 3,
                wchar: 4,
                pcpu: 50,
                volCtxt: 5,
                invCtxt: 6
        )

        when: "compute the progress of the workflow"
        def progress = progressService.getProgressQuery(wf)
        then:
        with(progress.workflowProgress) {
            pending==0
            running==0
            submitted==0
            succeeded==1
            failed==0
            cached==0
            cpus == 1
            cpuTime == 2
            cpuLoad == 1
            memoryRss == 1
            memoryReq == 2
            readBytes == 3
            writeBytes == 4
            volCtxSwitch == 5
            invCtxSwitch == 6
            cpuEfficiency == 50.0F
            memoryEfficiency == 50.0F
        }

        progress.processesProgress.size() ==1
        with(progress.processesProgress[0])  {
            pending==0
            running==0
            submitted==0
            succeeded==1
            failed==0
            cached==0
            cpus == 1
            cpuTime == 2
            cpuLoad == 1
            memoryRss == 1
            memoryReq == 2
            readBytes == 3
            writeBytes == 4
            volCtxSwitch == 5
            invCtxSwitch == 6
        }
    }


    void "compute return process with no tasks" () {
        given:
        def creator = new DomainCreator()

        Workflow wf = creator.createWorkflow()
        creator.createProcess(workflow: wf, name:'p1', position:0)
        creator.createProcess(workflow: wf, name:'p2', position:1)

        when: "compute the progress of the workflow"
        def progress = progressService.getProgressQuery(wf)
        then:
        with(progress.workflowProgress) {
            pending==0
            running==0
            submitted==0
            succeeded==0
            failed==0
            cached==0
            cpus == 0
            cpuTime == 0
            cpuLoad == 0
            memoryRss == 0
            memoryReq == 0
            readBytes == 0
            writeBytes == 0
            volCtxSwitch == 0
            invCtxSwitch == 0
            cpuEfficiency == 0
            memoryEfficiency == 0
        }

        progress.processesProgress.size() ==2
        and:
        with(progress.processesProgress[0])  {
            process == 'p1'
            pending==0
            running==0
            submitted==0
            succeeded==0
            failed==0
            cached==0
        }
        and:
        with(progress.processesProgress[1])  {
            process == 'p2'
            pending==0
            running==0
            submitted==0
            succeeded==0
            failed==0
            cached==0
        }
    }



    def 'should serialise progress' () {
        given:
        def progress = new WorkflowLoad()
        progress.running = 3L
        progress.succeeded = 4L
        progress.loadMemory = 10
        progress.cpuTime = 30

        when:
        def json = DomainHelper.toJson(progress)
        println json
        and:
        Map map = new JsonSlurper().parseText(json)
        then:
        map.cpuTime == 30 
        map.loadMemory == 10
        map.running == 3
        map.succeeded == 4
        map.containsKey('failed')
        !map.containsKey('aborted')
        !map.containsKey('taskCount')
    }


}

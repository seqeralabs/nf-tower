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

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.exchange.progress.ProcessProgress
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class ProgressServiceTest extends AbstractContainerBaseTest {

    @Inject
    ProgressService progressService

    void "compute simple progress" () {
        String process1 = 'process1'
        Task task1 = new DomainCreator().createTask(
                status: TaskStatus.COMPLETED,
                process: process1,
                cpus: 1,
                realtime: 2,
                peakRss: 1,
                memory: 2,
                rchar: 3,
                wchar: 4,
                pcpu: 25,
                volCtxt: 5,
                invCtxt: 6
        )

        when: "compute the progress of the workflow"
        def progress = progressService.computeWorkflowProgress(task1.workflow.id)
        then:
        with(progress.workflowProgress) {
            pending==0
            running==0
            submitted==0
            succeeded==1
            failed==0
            cached==0
            totalCpus == 1
            cpuTime == 2
            cpuLoad == 0.5f
            memoryRss == 1
            memoryReq == 2
            readBytes == 3
            writeBytes == 4
            volCtxSwitch == 5
            invCtxSwitch == 6
            cpuEfficiency == 25.0d
            memoryEfficiency == 50.0d
        }

        progress.processesProgress.size() ==1
        with(progress.processesProgress[0])  {
            pending==0
            running==0
            submitted==0
            succeeded==1
            failed==0
            cached==0
            totalCpus == 1
            cpuTime == 2
            cpuLoad == 0.5f
            memoryRss == 1
            memoryReq == 2
            readBytes == 3
            writeBytes == 4
            volCtxSwitch == 5
            invCtxSwitch == 6
        }
    }

    void "compute the progress info of a workflow"() {
        given: 'create a pending task of a process and associated with a workflow (with some stats)'
        DomainCreator domainCreator = new DomainCreator()
        String process1 = 'process1'
        Task task1 = domainCreator.createTask(status: TaskStatus.NEW, process: process1, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2)
        Workflow workflow = task1.workflow

        and: 'a task for the previous process in each status (with some stats each one)'
        domainCreator.createTask(status: TaskStatus.SUBMITTED, workflow: workflow, process: process1, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2)
        domainCreator.createTask(status: TaskStatus.CACHED, workflow: workflow, process: process1, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2)
        domainCreator.createTask(status: TaskStatus.RUNNING, workflow: workflow, process: process1, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2)
        domainCreator.createTask(status: TaskStatus.FAILED, workflow: workflow, process: process1, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2)
        domainCreator.createTask(status: TaskStatus.COMPLETED, workflow: workflow, process: process1, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2)

        and: 'a pending task of another process (without stats)'
        String process2 = 'process2'
        domainCreator.createTask(status: TaskStatus.NEW, workflow: workflow, process: process2)

        and: 'a task for the previous process in each status (with some stats each one)'
        domainCreator.createTask(status: TaskStatus.SUBMITTED, workflow: workflow, process: process2, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2, volCtxt: 10, invCtxt: 30)
        domainCreator.createTask(status: TaskStatus.CACHED, workflow: workflow, process: process2, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2, volCtxt: 10, invCtxt: 30)
        domainCreator.createTask(status: TaskStatus.RUNNING, workflow: workflow, process: process2, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2, volCtxt: 10, invCtxt: 30)
        domainCreator.createTask(status: TaskStatus.FAILED, workflow: workflow, process: process2, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2, volCtxt: 10, invCtxt: 30)

        and: 'two more completed tasks (with some stats each one)'
        domainCreator.createTask(status: TaskStatus.COMPLETED, workflow: workflow, process: process2, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2, volCtxt: 10, invCtxt: 30)
        domainCreator.createTask(status: TaskStatus.COMPLETED, workflow: workflow, process: process2, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1, pcpu: 10, memory: 2, volCtxt: 10, invCtxt: 30)

        when: "compute the progress of the workflow"
        ProgressData progress = progressService.computeWorkflowProgress(workflow.id)

        then: "the tasks has been successfully computed"
        progress.workflowProgress.pending == 2
        progress.workflowProgress.submitted == 2
        progress.workflowProgress.running == 2
        progress.workflowProgress.cached == 2
        progress.workflowProgress.failed == 2
        progress.workflowProgress.succeeded == 3

        progress.workflowProgress.totalCpus == 12
        progress.workflowProgress.cpuTime == 24
        progress.workflowProgress.cpuLoad == 2.4d
        progress.workflowProgress.memoryRss == 12
        progress.workflowProgress.memoryReq == 24
        progress.workflowProgress.readBytes == 12
        progress.workflowProgress.writeBytes == 12
        progress.workflowProgress.volCtxSwitch == 60
        progress.workflowProgress.invCtxSwitch == 180

        progress.workflowProgress.cpuEfficiency == 10.0d
        progress.workflowProgress.memoryEfficiency == 50.0d


        then: "the processes progress has been successfully computed"
        progress.processesProgress.size() == 2
        ProcessProgress progress1 = progress.processesProgress.find { it.process == process1 }
        progress1.running == 1
        progress1.submitted == 1
        progress1.failed == 1
        progress1.pending == 1
        progress1.succeeded == 1
        progress1.cached == 1

        progress1.cpuTime == 12
        progress1.cpuLoad == 1.2d
        progress1.totalCpus == 6
        progress1.memoryRss == 6
        progress1.readBytes == 6
        progress1.writeBytes == 6
        progress1.volCtxSwitch == 0
        progress1.invCtxSwitch == 0

        ProcessProgress progress2 = progress.processesProgress.find { it.process == process2 }
        progress2.running == 1
        progress2.submitted == 1
        progress2.failed == 1
        progress2.pending == 1
        progress2.succeeded == 2
        progress2.cached == 1

        progress2.cpuTime == 12
        progress2.cpuLoad == 1.2d
        progress2.totalCpus == 6
        progress2.memoryRss == 6
        progress2.memoryReq == 12
        progress2.readBytes == 6
        progress2.writeBytes == 6
        progress2.volCtxSwitch == 60
        progress2.invCtxSwitch == 180
    }

}

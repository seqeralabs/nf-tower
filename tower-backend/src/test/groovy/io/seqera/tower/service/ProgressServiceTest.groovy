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
import io.seqera.tower.domain.ProcessProgress
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.progress.ProgressGet
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class ProgressServiceTest extends AbstractContainerBaseTest {

    @Inject
    ProgressService progressService


    void "compute the progress info of a workflow"() {
        given: 'create a pending task of a process and associated with a workflow (with some stats)'
        DomainCreator domainCreator = new DomainCreator()
        String process1 = 'process1'
        Task task1 = domainCreator.createTask(status: TaskStatus.NEW, process: process1, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1)
        Workflow workflow = task1.workflow

        and: 'a task for the previous process in each status (with some stats each one)'
        [TaskStatus.SUBMITTED, TaskStatus.CACHED, TaskStatus.RUNNING, TaskStatus.FAILED, TaskStatus.COMPLETED].each { TaskStatus status ->
            domainCreator.createTask(status: status, workflow: workflow, process: process1, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1)
        }

        and: 'a pending task of another process (without stats)'
        String process2 = 'process2'
        domainCreator.createTask(status: TaskStatus.NEW, workflow: workflow, process: process2)

        and: 'a task for the previous process in each status (with some stats each one)'
        [TaskStatus.SUBMITTED, TaskStatus.CACHED, TaskStatus.RUNNING, TaskStatus.FAILED].each { TaskStatus status ->
            domainCreator.createTask(status: status, workflow: workflow, process: process2, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1)
        }

        and: 'two more completed tasks (with some stats each one)'
        2.times {
            domainCreator.createTask(status: TaskStatus.COMPLETED, workflow: workflow, process: process2, cpus: 1, realtime: 2, peakRss: 1, rchar: 1, wchar: 1)
        }

        when: "compute the progress of the workflow"
        ProgressGet progress = progressService.computeWorkflowProgress(workflow.id)

        then: "the tasks has been successfully computed"
        progress.workflowProgress.pending == 2
        progress.workflowProgress.submitted == 2
        progress.workflowProgress.running == 2
        progress.workflowProgress.cached == 2
        progress.workflowProgress.failed == 2
        progress.workflowProgress.succeeded == 3

        progress.workflowProgress.totalCpus == 12
        progress.workflowProgress.cpuRealtime == 24
        progress.workflowProgress.memory == 12
        progress.workflowProgress.diskReads == 12
        progress.workflowProgress.diskWrites == 12


        then: "the processes progress has been successfully computed"
        progress.processesProgress.size() == 2
        ProcessProgress progress1 = progress.processesProgress.find { it.process == process1 }
        progress1.running == 1
        progress1.submitted == 1
        progress1.failed == 1
        progress1.pending == 1
        progress1.succeeded == 1
        progress1.cached == 1

        progress1.cpuRealtime == 12
        progress1.totalCpus == 6
        progress1.memory == 6
        progress1.diskReads == 6
        progress1.diskWrites == 6

        ProcessProgress progress2 = progress.processesProgress.find { it.process == process2 }
        progress2.running == 1
        progress2.submitted == 1
        progress2.failed == 1
        progress2.pending == 1
        progress2.succeeded == 2
        progress2.cached == 1

        progress2.cpuRealtime == 12
        progress2.totalCpus == 6
        progress2.memory == 6
        progress2.diskReads == 6
        progress2.diskWrites == 6
    }

}

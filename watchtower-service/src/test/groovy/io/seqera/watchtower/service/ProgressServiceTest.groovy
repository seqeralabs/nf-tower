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

package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.ProcessProgress
import io.seqera.watchtower.domain.TasksProgress
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.pogo.exchange.progress.ProgressGet
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class ProgressServiceTest extends AbstractContainerBaseTest {

    @Inject
    ProgressService progressService


    void "compute the progress info of a workflow"() {
        given: 'create a pending task of a process and associated with a workflow'
        DomainCreator domainCreator = new DomainCreator()
        String process1 = 'process1'
        Task task1 = domainCreator.createTask(status: TaskStatus.NEW, process: process1, duration: 1)
        Workflow workflow = task1.workflow

        and: 'a task for the previous process in each status'
        [TaskStatus.SUBMITTED, TaskStatus.CACHED, TaskStatus.RUNNING, TaskStatus.FAILED].each { TaskStatus status ->
            domainCreator.createTask(status: status, workflow: workflow, process: process1, duration: 1)
        }

        and: 'one more completed task of the same process'
        domainCreator.createTask(status: TaskStatus.COMPLETED, workflow: workflow, process: process1, duration: 1, hash: "lastHash")

        and: 'a pending task of another process'
        String process2 = 'process2'
        domainCreator.createTask(status: TaskStatus.NEW, workflow: workflow, process: process2, duration: 1)

        and: 'a task for the previous process in each status'
        [TaskStatus.SUBMITTED, TaskStatus.CACHED, TaskStatus.RUNNING, TaskStatus.FAILED].each { TaskStatus status ->
            domainCreator.createTask(status: status, workflow: workflow, process: process2, duration: 1)
        }

        and: 'two more completed tasks'
        2.times {
            domainCreator.createTask(status: TaskStatus.COMPLETED, workflow: workflow, process: process2, duration: 1, hash: "lastHash${it}")
        }

        when: "compute the progress of the workflow"
        ProgressGet progress = progressService.computeWorkflowProgress(workflow.id)

        then: "the tasks has been successfully computed"
        progress.tasksProgress.pending == 2
        progress.tasksProgress.submitted == 2
        progress.tasksProgress.running == 2
        progress.tasksProgress.cached == 2
        progress.tasksProgress.failed == 2
        progress.tasksProgress.succeeded == 3

        then: "the processes progress has been successfully computed"
        progress.processesProgress.size() == 2
        ProcessProgress progress1 = progress.processesProgress.find { it.process == process1 }
        progress1.totalTasks == 6
        progress1.completedTasks == 1
        progress1.totalDuration == 6
        progress1.lastTaskHash == 'lastHash'
        ProcessProgress progress2 = progress.processesProgress.find { it.process == process2 }
        progress2.totalTasks == 7
        progress2.completedTasks == 2
        progress2.totalDuration == 7
        progress2.lastTaskHash == 'lastHash1'
    }

}

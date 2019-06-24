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
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.TaskStatus
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class ProgressServiceTest extends AbstractContainerBaseTest {

    @Inject
    ProgressService progressService


    void "compute the tasks progress info of a workflow"() {
        given: 'create a pending task associated with a workflow'
        DomainCreator domainCreator = new DomainCreator()
        Task task1 = domainCreator.createTask(status: TaskStatus.NEW)
        Workflow workflow = task1.workflow

        and: 'two tasks of each kind associated with the workflow'
        [TaskStatus.SUBMITTED, TaskStatus.CACHED, TaskStatus.RUNNING, TaskStatus.COMPLETED, TaskStatus.FAILED].eachWithIndex { TaskStatus status, int i ->
            2.times {
                domainCreator.createTask(status: status, workflow: workflow)
            }
        }

        when: "compute the tasks progress of the workflow"
        Progress progress = progressService.computeProgress(workflow.id)

        then: "the progress has been successfully computes"
        progress.pending == 1
        progress.submitted == 2
        progress.running == 2
        progress.cached == 2
        progress.failed == 2
        progress.succeeded == 2
    }



}

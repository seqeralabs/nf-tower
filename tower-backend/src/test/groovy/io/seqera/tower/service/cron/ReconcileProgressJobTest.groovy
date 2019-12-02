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

package io.seqera.tower.service.cron

import javax.inject.Inject
import java.time.OffsetDateTime

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.enums.WorkflowStatus
import io.seqera.tower.service.progress.ProgressOperationsImpl
import io.seqera.tower.util.DomainCreator
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Transactional
@MicronautTest(application = Application)
class ReconcileProgressJobTest extends Specification {

    @Inject ReconcileProgressJob svc
    @Inject TransactionService tx
    @Inject ProgressOperationsImpl progressOp

    def 'should find workflow with missing progress' () {
        given:
        def creator = new DomainCreator()
        def user = creator.createUser()
        def w1 = creator.createWorkflow(owner: user, id: 'abc', status: WorkflowStatus.SUCCEEDED, complete: OffsetDateTime.now().minusHours(2))
        def w2 = creator.createWorkflow(owner: user, id: 'ABC', status: WorkflowStatus.SUCCEEDED, complete: OffsetDateTime.now().minusHours(2))
        creator.createWorkflowLoad(workflow: w2)

        when:
        def wf = tx.withNewTransaction { svc.findWorkflowWithMissingProgress() }
        then:
        wf.id == w1.id

        when:
        tx.withNewTransaction { svc.saveLoadRecords(wf) }
        then:
        tx.withNewTransaction { WorkflowLoad.findByWorkflow(wf) } != null
        and:
        tx.withNewTransaction { svc.findWorkflowWithMissingProgress() } == null
    }


    def 'should find only one with progress' () {
        given:
        def creator = new DomainCreator()
        def user = creator.createUser()
        creator.createWorkflow(owner: user, id: 'x1', status: WorkflowStatus.SUCCEEDED, complete: OffsetDateTime.now())
        creator.createWorkflow(owner: user, id: 'x2', status: WorkflowStatus.SUCCEEDED, complete: OffsetDateTime.now())
        creator.createWorkflow(owner: user, id: 'x3', status: WorkflowStatus.SUCCEEDED, complete: OffsetDateTime.now().minusHours(2))

        when:
        def wf = tx.withNewTransaction { svc.findWorkflowWithMissingProgress() }
        then:
        wf.id == 'x3'
    }

}

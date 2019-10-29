package io.seqera.tower.service.cron

import javax.inject.Inject

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
class CronServiceTest extends Specification {

    @Inject TransactionService tx
    @Inject CronServiceImpl cron
    @Inject ProgressOperationsImpl progressOp

    def 'should find workflow with missing progress' () {
        given:
        def creator = new DomainCreator()
        def user = creator.createUser()
        def w1 = creator.createWorkflow(owner: user, id: 'abc', status: WorkflowStatus.SUCCEEDED)
        def w2 = creator.createWorkflow(owner: user, id: 'ABC', status: WorkflowStatus.SUCCEEDED)
        creator.createWorkflowLoad(workflow: w2)

        when:
        def wf = tx.withNewTransaction { cron.findWorkflowWithMissingProgress() }
        then:
        wf.id == w1.id

        when:
        tx.withNewTransaction { cron.saveLoadRecords(wf) }
        then:
        tx.withNewTransaction { WorkflowLoad.findByWorkflow(wf) } != null
        and:
        tx.withNewTransaction { cron.findWorkflowWithMissingProgress() } == null

    }

}

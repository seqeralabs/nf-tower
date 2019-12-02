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
import javax.inject.Singleton
import java.time.Duration
import java.time.OffsetDateTime

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.seqera.tower.domain.Workflow
import io.seqera.tower.service.progress.ProgressOperationsImpl
import io.seqera.tower.service.progress.ProgressServiceImpl
import io.seqera.util.TupleUtils
import org.apache.commons.collections.MapUtils
import org.springframework.transaction.annotation.Propagation

/**
 * Check for workflow with missing progress records and creates it
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class ReconcileProgressJob implements CronJob {

    @Value('${tower.cron.progress-stats.delay:`10s`}')
    Duration initialDelay

    @Value('${tower.cron.progress-stats.interval:`1m`}')
    Duration schedulerDelay

    @Value('${tower.cron.progress-stats.enabled:`true`}')
    boolean enabled

    @Inject
    ProgressServiceImpl progressSvc

    @Inject
    ProgressOperationsImpl progressOp

    int progressSaveCounter

    @Override
    Boolean getEnabled() {
        return enabled
    }

    @Override
    Duration getDelay() {
        return initialDelay
    }

    @Override
    Duration getInterval() {
        return schedulerDelay
    }

    @Override
    Runnable getJob() {
        log.info "+ Creating workflow reconcile cron job -- initial-delay=$initialDelay; scheduler-delay=$schedulerDelay"
        return { saveLoadRecords() } as Runnable
    }

    protected void saveLoadRecords() {
        try {
            saveRecordsTx()
        }
        catch( Exception e ) {
            log.error "Unexpected error while save load records | ${e.message}"
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void saveRecordsTx() {
        final workflow = findWorkflowWithMissingProgress()
        if( workflow != null ) {
            saveLoadRecords(workflow)
        }
    }

    protected void saveLoadRecords(Workflow workflow) {
        log.debug "Reconciling execution progress for workflow Id=$workflow.id (${++progressSaveCounter})"
        def data = progressSvc.getProgressQuery(workflow)
        progressOp.persistProgressData(workflow, data)
        log.debug "Reconciling execution progress for workflow Id=${workflow.id} DONE"
    }

    protected Workflow findWorkflowWithMissingProgress() {
        final args = TupleUtils.map('max', 1)
        final params = TupleUtils.map('ts', OffsetDateTime.now().minusHours(1))
        def query = "from Workflow w where w.status != 'RUNNING' and complete < :ts and w.id not in (select distinct l.workflow.id from WorkflowLoad l)"
        Workflow.find(query,params,args)
    }

}

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

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.seqera.tower.service.WorkflowService
import io.seqera.tower.service.audit.AuditEventPublisher
import org.springframework.transaction.annotation.Propagation
/**
 * Permanently remove workflow marked for deletion
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
@Slf4j
@CompileStatic
class WorkflowDeleteJob implements CronJob {

    @Inject
    WorkflowService workflowService

    @Value('${tower.cron.workflow-delete.delay:`15s`}')
    Duration initialDelay

    @Value('${tower.cron.workflow-delete.interval:`30s`}')
    Duration schedulerDelay

    @Value('${tower.cron.workflow-delete.enabled:`true`}')
    boolean enabled

    @Inject AuditEventPublisher eventPublisher
    
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
        log.info "+ Creating workflow deletion cron job -- initial-delay=$initialDelay; scheduler-delay=$schedulerDelay"
        return {deleteWorkflowMarkedForDeletion()} as Runnable
    }

    protected void deleteWorkflowMarkedForDeletion() {
        try {
            log.trace "Checking workflow marked for deletion"
            deleteWorkflowMarkedForDeletion0()
        }
        catch( Exception e ) {
            log.debug "Unexpected error dropping deleted workflow | ${e.message ?: e}"
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    protected void deleteWorkflowMarkedForDeletion0() {
        def workflow = workflowService.findMarkedForDeletion(1)
        if( workflow ) {
            log.info "Dropping workflow marked for deletion=$workflow.id"
            workflowService.delete(workflow[0])
            eventPublisher.workflowDropped(workflow[0].id)
        }
    }
}

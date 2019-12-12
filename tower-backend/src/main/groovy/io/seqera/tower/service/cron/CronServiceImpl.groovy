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
import javax.inject.Named
import javax.inject.Singleton

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.TaskScheduler
import io.seqera.tower.service.mail.MailService
/**
 * Implements basic cron service to execute tasks at scheduled time
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class CronServiceImpl implements CronService {

    @Inject MailService mailService
    @Inject ReconcileProgressJob reconcileProgressCronJob
    @Inject WorkflowDeleteJob workflowDeleteCronJob
    @Inject WorkflowWatchdogJob workflowWatchdogJob

    @Inject @Named(TaskExecutors.SCHEDULED)
    TaskScheduler taskScheduler

    @Override
    void start() {
        start0()
    }

    private void start0() {
        log.info "+ Starting cron service [${this.getClass().getSimpleName()}]"
        // start mail service
        mailService.start()
        // start scheduler jobs
        createJobs()
    }

    protected void createJobs() {
        addJob(workflowDeleteCronJob)
        addJob(reconcileProgressCronJob)
        addJob(workflowWatchdogJob)
    }

    protected void addJob(CronJob job) {
        if( job.enabled )
            taskScheduler.scheduleWithFixedDelay(
                    job.getDelay(),
                    job.getInterval(),
                    job.getJob() )
    }

    @Override
    void stop() {
        stop0()
    }

    private void stop0() {
        log.debug "+ Stopping cron service"
        try {
            mailService.stop()
        }
        catch (Exception e ) {
            log.error("Error shutting down scheduler service | ${e.message}")
        }
    }

}

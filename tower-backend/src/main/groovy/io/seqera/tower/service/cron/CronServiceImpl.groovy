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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
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

    ScheduledExecutorService scheduler

    @Value('${tower.cron.poolSize:2}')
    int threadPoolSize

    @Inject MailService mailService
    @Inject ReconcileProgressJob reconcileProgressCronJob
    @Inject WorkflowDeleteJob workflowDeleteCronJob
    @Inject WorkflowWatchdogJob workflowWatchdogJob

    @Override
    void start() {
        start0()
    }

    private void start0() {
        log.info "+ Starting cron service [${this.getClass().getSimpleName()}]"
        // start mail service
        mailService.start()
        // start scheduler jobs
        scheduler = Executors.newScheduledThreadPool(threadPoolSize)
        createJobs()
    }

    protected void createJobs() {
        addJob(workflowDeleteCronJob)
        addJob(reconcileProgressCronJob)
        addJob(workflowWatchdogJob)
    }

    protected void addJob(CronJob job) {
        if( job.enabled )
            scheduler.scheduleWithFixedDelay(
                    job.getJob(),
                    job.getDelay().toMillis(),
                    job.getInterval().toMillis(),
                    TimeUnit.MILLISECONDS)
    }

    @Override
    void stop() {
        stop0()
    }

    private void stop0() {
        log.debug "+ Stopping cron service"
        try {
            mailService.stop()
            scheduler.shutdownNow()
            scheduler.awaitTermination(30,TimeUnit.SECONDS)
        }
        catch (Exception e ) {
            log.error("Error shutting down scheduler service | ${e.message}")
        }
    }

}

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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.seqera.tower.service.progress.ProgressService
/**
 * Cron job that checks periodically for stalled workflow executions
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class WorkflowWatchdogJob implements CronJob {

    @Value('${tower.cron.watchdog.delay:`5s`}')
    Duration initialDelay

    @Value('${tower.cron.watchdog.interval:`1m`}')
    Duration metricsInterval

    @Value('${tower.cron.watchdog.expire:`190s`}')
    Duration expireTimeout

    @Value('${tower.cron.watchdog.zombie:`1d`}')
    Duration zombieTimeout

    @Inject
    ProgressService progressService


    void run() {
        log.trace "Checking for expired workflow"
        progressService.checkForExpiredWorkflow(expireTimeout, zombieTimeout)
    }

    @Override
    Runnable getJob() {
        log.info "+ Creating workflow watchdog job -- metrics-interval=${metricsInterval}; expire-timeout=${expireTimeout}; zombie-timeout=${zombieTimeout}"
        return { run() } as Runnable
    }

    @Override
    Boolean getEnabled() {
        return true
    }

    @Override
    Duration getDelay() {
        return initialDelay
    }

    @Override
    Duration getInterval() {
        return metricsInterval
    }


}

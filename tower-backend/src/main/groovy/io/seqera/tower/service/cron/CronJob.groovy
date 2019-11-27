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

import java.time.Duration
/**
 * Cron job basic interface
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
interface CronJob {

    /**
     * @return The cron job as {@link Runnable} object
     */
    Runnable getJob()

    /**
     * @return {@code true} when the cron needs to be activated, {@code false} otherwise
     */
    Boolean getEnabled()

    /**
     * @return Initial job delay as {@link Duration} object
     */
    Duration getDelay()

    /**
     * @return Job interval between job executions as {@link Duration} object
     */
    Duration getInterval()

}

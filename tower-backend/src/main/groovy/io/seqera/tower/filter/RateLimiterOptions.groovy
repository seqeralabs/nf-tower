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

package io.seqera.tower.filter


import java.time.Duration

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.ConfigurationProperties
/**
 * Rate limiter config options
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
@ConfigurationProperties("tower.rate-limiter")
class RateLimiterOptions {

    Duration timeoutDuration

    Duration limitRefreshPeriod

    Integer limitForPeriod

    boolean isDisabled() {
        timeoutDuration==null && limitRefreshPeriod==null && limitForPeriod==null
    }

    void validate() {
        assert limitForPeriod>0, "Rate-limiter limitForPeriod must be greater than zero"
    }
}

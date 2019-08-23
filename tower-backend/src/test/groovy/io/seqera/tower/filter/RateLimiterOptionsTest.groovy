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

import javax.inject.Inject

import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(environments = ['ratelim'])
class RateLimiterOptionsTest extends Specification {

    @Inject
    RateLimiterOptions rateLimiterOptions
    
    def 'should load config' ( ){
        // check values in the `application-ratelim.yml` config file
        expect:
        rateLimiterOptions.timeoutDuration.toMillis() == 100
        rateLimiterOptions.limitRefreshPeriod.toMillis() == 1000
        rateLimiterOptions.limitForPeriod == 5 
    }
    
}

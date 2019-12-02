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
import java.time.Duration

import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class WorkflowWatchdogJobTest extends Specification {

    @Inject WorkflowWatchdogJob job


    def 'should check timeout values' () {
        expect:
        job.expireTimeout == Duration.parse('PT3M10S')
        job.zombieTimeout == Duration.parse('PT24H')
    }


}

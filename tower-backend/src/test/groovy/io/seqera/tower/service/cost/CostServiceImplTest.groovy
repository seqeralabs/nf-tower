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

package io.seqera.tower.service.cost

import javax.inject.Inject

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Unroll
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class CostServiceImplTest extends Specification {

    @Inject CostServiceImpl svc

    @Unroll
    @Property(name='tower.costs.local.costPerCpuHour', value = '1')
    def 'should compute cost' () {

        expect:
        svc.computeCost(SECONDS*1000, CPUS) == COST

        where:
        CPUS    | SECONDS   | COST
        1       | 3600      | 1
        2       | 3600      | 2
        3       | 3600      | 3
        1       | 1800      | 0.5
        2       | 1800      | 1
        10      | 1         | 0.0027777778

    }
}

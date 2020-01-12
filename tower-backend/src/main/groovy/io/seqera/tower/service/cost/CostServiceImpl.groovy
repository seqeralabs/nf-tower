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

import javax.inject.Singleton

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.seqera.tower.domain.Task

/**
 * Compute the task computation cost
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@Singleton
class CostServiceImpl implements CostService {

    @Value('${tower.service.costCpuHour:0.1}')
    BigDecimal costCpuHour

    @Override
    BigDecimal computeCost(Task task) {
        computeCost(task.getRealtime() ?: 0, task.getCpus() ?: 1)
    }

    protected BigDecimal computeCost(long millis, int cpus) {
        return  millis * cpus * costCpuHour / 3_600_000
    }
}

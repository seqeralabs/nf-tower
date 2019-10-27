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

package io.seqera.tower.endpoints

import javax.inject.Inject

import io.micronaut.management.endpoint.annotation.Endpoint
import io.micronaut.management.endpoint.annotation.Read
import io.seqera.tower.service.progress.ProgressService
/**
 * Expose current load stats
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Endpoint('stats')
class StatsEndpoint {

    @Inject
    ProgressService progressService

    @Read
    Map getLoad() {
        final list = progressService.getStats()
        final result = [
            count:list.size(),
            workflows: list
        ]
        return result
    }

}

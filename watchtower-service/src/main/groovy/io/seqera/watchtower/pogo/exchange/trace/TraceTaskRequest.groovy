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

package io.seqera.watchtower.pogo.exchange.trace

import com.fasterxml.jackson.annotation.JsonSetter
import groovy.transform.ToString
import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.Task

import java.time.Instant

/**
 * Model a Trace workflow request
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceTaskRequest {

    Task task
    Progress progress

    Instant utcTime


    @JsonSetter('utcTime')
    void deserializeCompleteInstant(String utcTimestamp) {
        utcTime = utcTimestamp ? Instant.parse(utcTimestamp) : null
    }

}

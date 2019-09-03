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

package io.seqera.tower.service

import io.micronaut.http.sse.Event
import io.reactivex.Flowable

import java.time.Duration

interface ServerSentEventsService {

    Flowable getOrCreate(String key, Duration idleTimeout, Duration throttleTime)

    void tryPublish(String key, Closure<Event> payload)

    void tryComplete(String key)

    Flowable generateHeartbeatFlowable(Duration interval, Closure<Event> heartbeatGenerator)

}

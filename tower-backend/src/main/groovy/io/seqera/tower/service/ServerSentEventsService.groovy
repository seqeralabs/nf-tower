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

import java.time.Duration

import io.micronaut.http.sse.Event
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor

interface ServerSentEventsService {

    Flowable getOrCreateUserPublisher(Serializable userId)

    Flowable getOrCreateWorkflowPublisher(Serializable workflowId)

    String getKeyForEntity(Class aClass, def id)

    Flowable getOrCreatePublisher(String key, Duration idleTimeout, Closure<Event> idleTimeoutLastEvent, Duration throttleTime)

    void tryPublish(String key, Closure<Event> payload)

    void tryComplete(String key)

    Flowable generateHeartbeatFlowable(Duration interval, Closure<Event> heartbeatGenerator)

    Flowable getOrCreateHeartbeatForPublisher(PublishProcessor<Event> publisher, Closure<Event> heartbeatGenerator)

}

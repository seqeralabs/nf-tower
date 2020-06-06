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

package io.seqera.tower.controller

import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.sse.Event
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Flowable
import io.seqera.tower.exchange.live.LiveUpdate
import io.seqera.tower.service.live.LiveEventsService
import io.seqera.tower.service.UserService
import org.reactivestreams.Publisher
/**
 * Server Sent Events endpoints to receive live updates in the client.
 *
 */
@Slf4j
@CompileStatic
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/live")
class LiveEventsController {

    UserService userService
    LiveEventsService serverSentEventsService

    @Inject
    LiveEventsController(UserService userService, LiveEventsService serverSentEventsService) {
        this.userService = userService
        this.serverSentEventsService = serverSentEventsService
    }


    @Get("/")
    Publisher<Event<List<LiveUpdate>>> live(HttpRequest request) {
        log.debug("== Client subscribing to live events [remoteAddress=${request.remoteAddress}]")
        try {
            return serverSentEventsService.getEventPublisher()
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter"
            log.error("${message} | ${e.message}", e)

            return Flowable.just(Event.of([LiveUpdate.ofError(message)]))
        }
    }

}

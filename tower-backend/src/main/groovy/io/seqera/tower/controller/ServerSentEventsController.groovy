package io.seqera.tower.controller

import javax.inject.Inject

import groovy.util.logging.Slf4j
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.sse.Event
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Flowable
import io.seqera.tower.exchange.trace.sse.TraceSseResponse
import io.seqera.tower.service.ServerSentEventsService
import io.seqera.tower.service.UserService
import org.reactivestreams.Publisher
/**
 * Server Sent Events endpoints to receive live updates in the client.
 *
 */
@Controller("/sse")
@Secured(SecurityRule.IS_ANONYMOUS)
@Slf4j
class ServerSentEventsController {

    UserService userService
    ServerSentEventsService serverSentEventsService

    @Inject
    ServerSentEventsController(UserService userService, ServerSentEventsService serverSentEventsService) {
        this.userService = userService
        this.serverSentEventsService = serverSentEventsService
    }


    @Get("/")
    Publisher<Event<List<TraceSseResponse>>> live() {
        log.debug("Subscribing to live events")
        try {
            return serverSentEventsService.eventsFlowable
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter"
            log.error("${message} | ${e.message}", e)

            return Flowable.just(Event.of([TraceSseResponse.ofError(null, null, message)]))
        }
    }

}

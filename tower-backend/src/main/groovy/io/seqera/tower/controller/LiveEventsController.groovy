package io.seqera.tower.controller

import javax.inject.Inject

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.sse.Event
import io.micronaut.security.annotation.Secured
import io.reactivex.Flowable
import io.seqera.tower.exchange.live.LiveUpdate
import io.seqera.tower.service.LiveEventsService
import io.seqera.tower.service.UserService
import org.reactivestreams.Publisher
/**
 * Server Sent Events endpoints to receive live updates in the client.
 *
 */
@Controller("/live")
@Secured(['ROLE_USER'])
@Slf4j
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
        log.debug("Client subscribing to live events [remoteAddress=${request.remoteAddress}]")
        try {
            return serverSentEventsService.getEventsFlowable()
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter"
            log.error("${message} | ${e.message}", e)

            return Flowable.just(Event.of([LiveUpdate.ofError(message)]))
        }
    }

}

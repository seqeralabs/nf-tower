package io.seqera.tower.controller

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.sse.Event
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Flowable
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.enums.SseErrorType
import io.seqera.tower.exchange.trace.sse.TraceSseResponse
import io.seqera.tower.service.ServerSentEventsService
import io.seqera.tower.service.UserService
import org.reactivestreams.Publisher

import javax.inject.Inject
import java.time.Duration

@Controller("/sse")
@Secured(SecurityRule.IS_ANONYMOUS)
@Slf4j
class ServerSentEventsController {

    @Value('${sse.time.idle.workflow:5m}')
    Duration idleWorkflowFlowableTimeout

    @Value('${sse.time.idle.user:5m}')
    Duration idleUserFlowableTimeout

    @Value('${sse.time.heartbeat.user:1m}')
    Duration heartbeatUserFlowableInterval

    UserService userService

    ServerSentEventsService serverSentEventsService

    @Inject
    ServerSentEventsController(ServerSentEventsService serverSentEventsService) {
        this.userService = userService
        this.serverSentEventsService = serverSentEventsService
    }


    @Get("/workflow/{workflowId}")
    Publisher<Event<TraceSseResponse>> liveWorkflow(String workflowId) {
        String workflowFlowableKey = serverSentEventsService.getKeyForEntity(Workflow, workflowId)

        log.info("Subscribing to live events of workflow: ${workflowFlowableKey}")
        Flowable<Event<TraceSseResponse>> workflowFlowable
        try {
            workflowFlowable = serverSentEventsService.getOrCreate(workflowFlowableKey, idleWorkflowFlowableTimeout,
                     { Event.of(TraceSseResponse.ofError(SseErrorType.TIMEOUT, "Expired [${workflowFlowableKey}]")) }, null)
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter: ${workflowFlowableKey}"
            log.error("${message} | ${e.message}", e)
            workflowFlowable = Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        return workflowFlowable
    }

    @Get("/user/{userId}")
    Publisher<Event<TraceSseResponse>> liveUser(Long userId) {
        String userFlowableKey = serverSentEventsService.getKeyForEntity(User, userId)

        log.info("Subscribing to live events of user: ${userFlowableKey}")
        Flowable<Event<TraceSseResponse>> userFlowable
        try {
            userFlowable = serverSentEventsService.getOrCreate(userFlowableKey, idleUserFlowableTimeout,
                    { Event.of(TraceSseResponse.ofError(SseErrorType.TIMEOUT, "Expired [${userFlowableKey}]")) }, null)
        }
        catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter: ${userFlowableKey}"
            log.error("${message} | ${e.message}", e)

            return Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        Flowable heartbeatUserFlowable = serverSentEventsService.generateHeartbeatFlowable(heartbeatUserFlowableInterval, {
            log.info("Server heartbeat ${it} generated for flowable: ${userFlowableKey}")
            Event.of(TraceSseResponse.ofHeartbeat("Server heartbeat [${userFlowableKey}]"))
        })

        return userFlowable.mergeWith(heartbeatUserFlowable)
                           .takeUntil(userFlowable.takeLast(1))
    }

}

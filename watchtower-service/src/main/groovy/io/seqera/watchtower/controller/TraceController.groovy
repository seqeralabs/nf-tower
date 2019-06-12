package io.seqera.watchtower.controller

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.sse.Event
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.reactivex.Flowable
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.SseErrorType
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskResponse
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowRequest
import io.seqera.watchtower.pogo.exchange.trace.TraceWorkflowResponse
import io.seqera.watchtower.pogo.exchange.trace.sse.TraceSseResponse
import io.seqera.watchtower.service.ServerSentEventsService
import io.seqera.watchtower.service.TraceService
import org.reactivestreams.Publisher

import javax.inject.Inject

/**
 * Implements the `trace` API
 *
 */
@Controller("/trace")
@Secured(SecurityRule.IS_ANONYMOUS)
@Slf4j
class TraceController {

    TraceService traceService
    ServerSentEventsService serverSentEventsService

    @Inject
    TraceController(TraceService traceService, ServerSentEventsService serverSentEventsService) {
        this.traceService = traceService
        this.serverSentEventsService = serverSentEventsService
    }


    @Post("/workflow")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceWorkflowResponse> workflow(@Body TraceWorkflowRequest trace) {
        HttpResponse<TraceWorkflowResponse> response
        try {
            log.info("Receiving workflow trace: ${trace.inspect()}")
            Workflow workflow = traceService.processWorkflowTrace(trace)
            log.info("Processed workflow trace ${workflow.id}")

            response = HttpResponse.created(TraceWorkflowResponse.ofSuccess(workflow.id.toString()))

            publishWorkflowEvent(workflow)
        } catch (Exception e) {
            response = HttpResponse.badRequest(TraceWorkflowResponse.ofError(e.message))
        }

        response
    }

    private void publishWorkflowEvent(Workflow workflow) {
        if (workflow.checkIsStarted()) {
            serverSentEventsService.createFlowable(workflow.id.toString())
        }

        try {
            serverSentEventsService.publishEvent(workflow.id.toString(), Event.of(TraceSseResponse.ofWorkflow(workflow)))
        } catch (NonExistingFlowableException e) {
            log.error("No flowable found for id while trying to publish workflow data: ${workflow.id}")
        }

        if (!workflow.checkIsStarted()) {
            serverSentEventsService.completeFlowable(workflow.id.toString())
        }
    }

    private void publishErrorEvent(String workflowId, String errorMessage) {
        TraceSseResponse errorResponse = TraceSseResponse.ofError(SseErrorType.BAD_PROCESSING, errorMessage)
        try {
            serverSentEventsService.publishEvent(workflowId, Event.of(errorResponse))
        } catch (NonExistingFlowableException e) {
            log.error("No flowable found for id while trying to publish error data: ${workflowId}")
        }
    }

    @Post("/task")
    @Transactional
    @Secured(['ROLE_USER'])
    HttpResponse<TraceTaskResponse> task(@Body TraceTaskRequest trace) {
        HttpResponse<TraceTaskResponse> response
        try {
            log.info("Receiving task trace: ${trace.inspect()}")
            Task task = traceService.processTaskTrace(trace)
            log.info("Processed task trace ${task.id} (${task.taskId} ${task.status.name()})")

            response = HttpResponse.created(TraceTaskResponse.ofSuccess(task.workflowId.toString()))
            publishTaskEvent(task)
        } catch (Exception e) {
            response = HttpResponse.badRequest(TraceTaskResponse.ofError(e.message))
        }

        response
    }

    private void publishTaskEvent(Task task) {
        try {
            serverSentEventsService.publishEvent(task.workflowId.toString(), Event.of(TraceSseResponse.ofTask(task)))
        } catch (NonExistingFlowableException e) {
            log.error("No flowable found for id while trying to publish task data: ${task.workflowId}")
        }
    }

    @Get("/live/{workflowId}")
    Publisher<Event<TraceSseResponse>> live(Long workflowId) {
        log.info("Subscribing to live events of workflow: ${workflowId}")

        Flowable<Event<TraceSseResponse>> flowable
        try {
            flowable = serverSentEventsService.getFlowable(workflowId.toString())
        } catch (NonExistingFlowableException e) {
            String message = "No live events emitter for workflow: ${workflowId}"
            log.info(message)
            flowable = Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.NONEXISTENT, message)))
        } catch (Exception e) {
            String message = "Unexpected error while obtaining event emitter for workflow: ${workflowId}"
            log.error("${message} | ${e.message}")
            flowable = Flowable.just(Event.of(TraceSseResponse.ofError(SseErrorType.UNEXPECTED, message)))
        }

        flowable
    }

}
package io.seqera.watchtower.service

import io.micronaut.http.sse.Event
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.seqera.watchtower.pogo.exceptions.NonExistingFlowableException
import io.seqera.watchtower.pogo.exchange.live.LiveWorkflowUpdateMultiResponse

import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class LiveWorkflowUpdateSseServiceImpl implements LiveWorkflowUpdateSseService {

    private Map<Long, PublishProcessor<Event<LiveWorkflowUpdateMultiResponse>>> flowableByWorkflowIdCache = new ConcurrentHashMap()


    void createFlowable(Long workflowId) {
        flowableByWorkflowIdCache[workflowId] = PublishProcessor.create()
    }/**/

    void publishUpdate(Long workflowId, LiveWorkflowUpdateMultiResponse data) {
        PublishProcessor hotFlowable = (PublishProcessor) getFlowable(workflowId)

        hotFlowable.onNext(Event.of(data))
    }

    void completeFlowable(Long workflowId) {
        PublishProcessor hotFlowable = (PublishProcessor) getFlowable(workflowId)

        hotFlowable.onComplete()
        flowableByWorkflowIdCache.remove(workflowId)
    }

    Flowable getFlowable(Long workflowId) {
        Flowable hotFlowable = flowableByWorkflowIdCache[workflowId]

        if (!hotFlowable) {
            throw new NonExistingFlowableException("No flowable for workflow ${workflowId} exists")
        }

        hotFlowable
    }

}

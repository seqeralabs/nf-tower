package io.seqera.watchtower.service

import io.reactivex.Flowable
import io.seqera.watchtower.pogo.exchange.live.LiveWorkflowUpdateMultiResponse

interface LiveWorkflowUpdateSseService {

    void createFlowable(Long workflowId)

    void publishUpdate(Long workflowId, LiveWorkflowUpdateMultiResponse data)

    Flowable getFlowable(Long workflowId)

    void completeFlowable(Long workflowId)


}
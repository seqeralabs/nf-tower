package io.seqera.watchtower.service

import io.reactivex.Flowable
import io.seqera.watchtower.pogo.exchange.live.LiveWorkflowUpdateMultiResponse

interface ServerSentEventsService {

    void createFlowable(Long id)

    void publishData(Long id, def data)

    Flowable getFlowable(Long id)

    void completeFlowable(Long id)


}
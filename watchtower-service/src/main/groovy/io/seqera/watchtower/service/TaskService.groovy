package io.seqera.watchtower.service

import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest

interface TaskService {

    Task processTaskJsonTrace(TraceTaskRequest taskJson)

}
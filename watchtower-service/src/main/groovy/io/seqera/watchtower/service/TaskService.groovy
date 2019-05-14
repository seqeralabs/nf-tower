package io.seqera.watchtower.service

import io.seqera.watchtower.controller.TraceWorkflowRequest
import io.seqera.watchtower.domain.Task

interface TaskService {

    Task processTaskJsonTrace(TraceWorkflowRequest taskJson)

}
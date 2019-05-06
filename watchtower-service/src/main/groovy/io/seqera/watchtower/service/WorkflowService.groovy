package io.seqera.watchtower.service

import io.seqera.watchtower.domain.Workflow

interface WorkflowService {

    Workflow processWorkflowJsonTrace(Map workflowJson)

}
package io.seqera.watchtower.pogo.exchange.workflow

import io.seqera.watchtower.domain.Progress
import io.seqera.watchtower.domain.SummaryEntry
import io.seqera.watchtower.domain.Workflow

class WorkflowGet {

    Workflow workflow
    Progress progress
    List<SummaryEntry> summary

}

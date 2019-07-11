/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.watchtower.exchange.workflow


import io.seqera.watchtower.domain.SummaryEntry
import io.seqera.watchtower.domain.Workflow

class WorkflowGet {

    Workflow workflow
    io.seqera.watchtower.exchange.progress.ProgressGet progress
    List<SummaryEntry> summary

    static WorkflowGet of(Workflow workflow) {
        new WorkflowGet(workflow: workflow)
    }

}

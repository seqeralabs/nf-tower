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

package io.seqera.tower.exchange.trace.sse


import io.seqera.tower.enums.SseErrorType
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.workflow.WorkflowGet

import java.time.Instant

class TraceSseResponse {

    def userId
    def workflowId

    WorkflowGet workflow
    ProgressData progress

    SseError error

    static TraceSseResponse ofWorkflow(def userId, def workflowId, WorkflowGet workflow) {
        new TraceSseResponse(userId: userId, workflowId: workflowId, workflow: workflow)
    }

    static TraceSseResponse ofProgress(def userId, def workflowId, ProgressData progress) {
        new TraceSseResponse(userId: userId, workflowId: workflowId, progress: progress)
    }

    static TraceSseResponse ofError(def userId, def workflowId, SseErrorType type, String errorMessage) {
        SseError sseError = new SseError(type: type, message: errorMessage)

        new TraceSseResponse(userId: userId, workflowId: workflowId, error: sseError)
    }

}

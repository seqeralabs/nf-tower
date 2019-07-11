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

package io.seqera.watchtower.exchange.trace.sse


import io.seqera.watchtower.enums.SseErrorType
import io.seqera.watchtower.exchange.progress.ProgressGet
import io.seqera.watchtower.exchange.workflow.WorkflowGet

import java.time.Instant

class TraceSseResponse {

    WorkflowGet workflow
    ProgressGet progress
    SseHeartbeat heartbeat
    SseError error

    static TraceSseResponse ofWorkflow(WorkflowGet workflow) {
        new TraceSseResponse(workflow: workflow)
    }

    static TraceSseResponse ofProgress(ProgressGet progress) {
        new TraceSseResponse(progress: progress)
    }

    static TraceSseResponse ofError(SseErrorType type, String errorMessage) {
        SseError sseError = new SseError(type: type, message: errorMessage)

        new TraceSseResponse(error: sseError)
    }

    static TraceSseResponse ofHeartbeat(String message) {
        SseHeartbeat sseHeartbeat = new SseHeartbeat(message: message, timestamp: Instant.now())

        new TraceSseResponse(heartbeat: sseHeartbeat)
    }


}

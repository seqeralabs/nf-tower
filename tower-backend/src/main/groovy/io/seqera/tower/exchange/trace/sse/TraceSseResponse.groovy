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

    WorkflowGet workflow
    ProgressData progress
    SseHeartbeat heartbeat
    SseError error

    static TraceSseResponse ofWorkflow(WorkflowGet workflow) {
        new TraceSseResponse(workflow: workflow)
    }

    static TraceSseResponse ofProgress(ProgressData progress) {
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

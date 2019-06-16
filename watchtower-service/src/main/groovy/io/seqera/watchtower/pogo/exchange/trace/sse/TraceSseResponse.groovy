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

package io.seqera.watchtower.pogo.exchange.trace.sse

import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.domain.Workflow
import io.seqera.watchtower.pogo.enums.SseErrorType
import io.seqera.watchtower.pogo.exchange.task.TaskGet
import io.seqera.watchtower.pogo.exchange.workflow.WorkflowGet

class TraceSseResponse {

    WorkflowGet workflow
    TaskGet task
    SseError error


    static TraceSseResponse ofWorkflow(Workflow workflow) {
        new TraceSseResponse(workflow: WorkflowGet.of(workflow))
    }

    static TraceSseResponse ofTask(Task task) {
        new TraceSseResponse(task: TaskGet.of(task))
    }

    static TraceSseResponse ofError(SseErrorType type, String errorMessage) {
        SseError sseError = new SseError(type: type, message: errorMessage)

        new TraceSseResponse(error: sseError)
    }

}

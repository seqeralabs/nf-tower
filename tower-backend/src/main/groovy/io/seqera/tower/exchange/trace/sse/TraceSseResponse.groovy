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

import groovy.transform.EqualsAndHashCode
import io.seqera.tower.enums.WorkflowAction
import io.seqera.tower.exchange.BaseResponse

@EqualsAndHashCode
class TraceSseResponse implements BaseResponse {

    def userId
    def workflowId

    WorkflowAction action
    String message

    static TraceSseResponse ofAction(def userId, def workflowId, WorkflowAction action) {
        new TraceSseResponse(userId: userId, workflowId: workflowId, action: action)
    }

    static TraceSseResponse ofError(def userId, def workflowId, String errorMessage) {
        new TraceSseResponse(userId: userId, workflowId: workflowId, message: errorMessage)
    }

}

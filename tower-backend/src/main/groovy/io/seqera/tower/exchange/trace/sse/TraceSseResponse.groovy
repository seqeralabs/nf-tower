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
import groovy.transform.ToString
import io.seqera.tower.enums.WorkflowAction
import io.seqera.tower.exchange.BaseResponse

@ToString(ignoreNulls = true)
@EqualsAndHashCode
class TraceSseResponse implements BaseResponse {

    final Long userId
    final String workflowId
    final WorkflowAction action
    final String message

    TraceSseResponse(Long userId, String workflowId, WorkflowAction action)  {
        this.userId = userId
        this.workflowId = workflowId
        this.action = action
        this.message = null
    }

    TraceSseResponse(String message) {
        this.message = message
        this.userId = null
        this.workflowId = null
        this.action = null
    }

    static TraceSseResponse of(Long userId, String workflowId, WorkflowAction action=null) {
        new TraceSseResponse(userId, workflowId, action)
    }

    static TraceSseResponse ofError(String errorMessage) {
        new TraceSseResponse(errorMessage)
    }

    String toString() {
        def result = "TraceSseResponse[userId=$userId; workflowId=$workflowId; action=${action?.toString()}"
        if( message )
            result += "; message='$message'"
        result += "]"
        return result
    }
}

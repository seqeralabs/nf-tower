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

package io.seqera.tower.exchange.live

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.seqera.tower.enums.LiveAction
import io.seqera.tower.exchange.BaseResponse

@ToString(ignoreNulls = true)
@EqualsAndHashCode
class LiveUpdate implements BaseResponse {

    final Long userId
    final String workflowId
    final LiveAction action
    final String message

    LiveUpdate(Long userId, String workflowId, LiveAction action)  {
        this.userId = userId
        this.workflowId = workflowId
        this.action = action
        this.message = null
    }

    LiveUpdate(String message) {
        this.message = message
        this.userId = null
        this.workflowId = null
        this.action = null
    }

    static LiveUpdate of(Long userId, String workflowId, LiveAction action=null) {
        new LiveUpdate(userId, workflowId, action)
    }

    static LiveUpdate ofError(String errorMessage) {
        new LiveUpdate(errorMessage)
    }

    String toString() {
        def result = "LiveUpdate[userId=$userId; workflowId=$workflowId; action=${action?.toString()}"
        if( message )
            result += "; message='$message'"
        result += "]"
        return result
    }
}

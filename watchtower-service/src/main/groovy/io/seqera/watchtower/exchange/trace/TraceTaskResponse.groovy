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

package io.seqera.watchtower.exchange.trace

import groovy.transform.ToString
import io.seqera.watchtower.enums.TraceProcessingStatus

/**
 * Model a Trace workflow response
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
class TraceTaskResponse {

    TraceProcessingStatus status
    String message
    String workflowId


    static TraceTaskResponse ofSuccess(String workflowId) {
        new TraceTaskResponse(status: TraceProcessingStatus.OK, workflowId: workflowId)
    }

    static TraceTaskResponse ofError(String message) {
        new TraceTaskResponse(status: TraceProcessingStatus.KO, message: message)
    }

}

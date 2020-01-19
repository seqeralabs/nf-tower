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

package io.seqera.tower.exchange.trace

import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.seqera.tower.enums.TraceProcessingStatus
import io.seqera.tower.exchange.BaseResponse

/**
 * Model a Trace workflow progress response
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
@ToString(includeNames = true, includePackage = false)
class TraceProgressResponse implements BaseResponse{

    TraceProcessingStatus status
    String message
    String workflowId


    static TraceProgressResponse ofSuccess(String workflowId) {
        new TraceProgressResponse(status: TraceProcessingStatus.OK, workflowId: workflowId)
    }

    static TraceProgressResponse ofError(String message) {
        new TraceProgressResponse(status: TraceProcessingStatus.KO, message: message)
    }

}

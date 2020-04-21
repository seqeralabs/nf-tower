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

package io.seqera.tower.exchange.workflow

import groovy.transform.CompileStatic
import io.seqera.tower.exchange.BaseResponse

@CompileStatic
class ListWorkflowResponse implements BaseResponse {

    String message
    List<GetWorkflowResponse> workflows

    static ListWorkflowResponse of(List<GetWorkflowResponse> workflows) {
        new ListWorkflowResponse(workflows: workflows)
    }

    static ListWorkflowResponse error(String message) {
        return new ListWorkflowResponse(message:message)
    }
}

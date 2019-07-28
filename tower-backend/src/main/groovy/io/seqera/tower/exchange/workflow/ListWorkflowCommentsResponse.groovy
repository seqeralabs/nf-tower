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
import groovy.transform.ToString
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.exchange.BaseResponse
/**
 * Response object to retrieve workflow comments
 *
 * See {@link io.seqera.tower.controller.WorkflowController#listComments(java.io.Serializable)}
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
@CompileStatic
class ListWorkflowCommentsResponse implements BaseResponse {

    String message
    List<WorkflowComment> comments

    static of( List<WorkflowComment> comments ) {
        new ListWorkflowCommentsResponse(comments: new ArrayList<WorkflowComment>(comments))
    }
}

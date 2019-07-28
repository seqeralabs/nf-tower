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

import java.time.OffsetDateTime

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Request object for a comment update operation
 *
 * See {@link io.seqera.tower.controller.WorkflowController#updateComment(io.micronaut.security.authentication.Authentication, java.lang.Long, io.seqera.tower.exchange.workflow.UpdateWorkflowCommentRequest)}
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@ToString
@CompileStatic
class UpdateWorkflowCommentRequest {
    def commentId
    String text
    OffsetDateTime timestamp
}

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

/**
 * Request object for workflow comment delete operation
 *
 * See {@link io.seqera.tower.controller.WorkflowController#deleteComment(io.micronaut.security.authentication.Authentication, java.lang.Long, io.seqera.tower.exchange.workflow.DeleteWorkflowCommentRequest)}
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class DeleteWorkflowCommentRequest {
    def commentId
    OffsetDateTime timestamp
}

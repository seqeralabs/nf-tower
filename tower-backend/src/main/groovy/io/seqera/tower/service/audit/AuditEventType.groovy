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

package io.seqera.tower.service.audit

/**
 * Enumerates possible audit actions
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
enum AuditEventType {
    workflow_created,
    workflow_status_changed,
    workflow_completed,
    workflow_deleted,
    workflow_dropped,
    access_token_created,
    access_token_deleted,
    user_created,
    user_updated,
    user_deleted,
    user_sign_in

    @Override String toString() { super.toString() }
}

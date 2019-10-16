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

package io.seqera.tower.enums

import io.seqera.util.StringUtils

enum TaskStatus {

    NEW,        // just created
    SUBMITTED,  // submitted to scheduler, pending execution
    RUNNING,    // task execution started
    CACHED,     // task cached
    COMPLETED,  // completed successfully
    FAILED,     // completed with error
    ABORTED     // execution aborted

    static Collection<TaskStatus> findStatusesByRegex(String criteria) {
        if( !criteria )
            return Collections.<TaskStatus>emptyList()
        if( !criteria.contains('*') )
            criteria += '*'

        values().findAll { StringUtils.like(it.name(), criteria) }
    }

    String toString() { super.toString() }

    boolean isTerminated() {
        this == COMPLETED || this == FAILED || this == ABORTED
    }

}

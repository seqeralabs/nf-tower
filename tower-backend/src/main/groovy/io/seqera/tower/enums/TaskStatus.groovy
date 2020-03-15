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
    ABORTED;    // execution aborted

    private final static Map<String,TaskStatus> displayLabel

    static {
        // some statuses are displayed in the UI using a different
        // label e.g. NEW => PENDING
        displayLabel = new LinkedHashMap<>(10)
        displayLabel.PENDING = NEW
        displayLabel.SUBMITTED = SUBMITTED
        displayLabel.RUNNING = RUNNING
        displayLabel.CACHED = CACHED
        displayLabel.SUCCEEDED = COMPLETED
        displayLabel.FAILED = FAILED
        displayLabel.ABORTED = ABORTED
    }

    static Collection<TaskStatus> findStatusesByRegex(String criteria) {
        if( !criteria )
            return Collections.<TaskStatus>emptyList()
        if( !criteria.contains('*') )
            criteria += '*'

        displayLabel
                .entrySet()
                .findAll { entry -> StringUtils.like(entry.key, criteria) }
                .collect { entry -> entry.value }
    }

    static private List<TaskStatus> TERMINAL = [COMPLETED, FAILED, ABORTED, CACHED]

    String toString() { super.toString() }

    boolean isTerminated() { this in TERMINAL }

    boolean isRunning() { this == RUNNING }

}

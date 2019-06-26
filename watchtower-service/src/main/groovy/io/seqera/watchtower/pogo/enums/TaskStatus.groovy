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

package io.seqera.watchtower.pogo.enums

enum TaskStatus {

    NEW, SUBMITTED, RUNNING, CACHED, COMPLETED, FAILED

    String toProgressString() {
        if (this == NEW) {
            return 'pending'
        }
        if (this == COMPLETED) {
            return 'succeeded'
        }

        return name().toLowerCase()
    }

    static Collection<TaskStatus> findStatusesByRegex(String sqlRegex) {
        String regex = sqlRegex.toUpperCase().replaceAll('%', /.*/)

        values().findAll {
            it.name() ==~ regex
        } ?: []
    }

}

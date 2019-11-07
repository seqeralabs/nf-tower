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


import spock.lang.Specification
/**
 *
 *  @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */

class TaskStatusTest extends Specification {

    def 'should find statuses' () {

        expect:
        TaskStatus.findStatusesByRegex(STR) == EXPECTED

        where:
        STR         | EXPECTED
        null        | []
        'run'       | [TaskStatus.RUNNING]
        'C'         | [TaskStatus.CACHED]
        'c*'        | [TaskStatus.CACHED]
        'pend'      | [TaskStatus.NEW]
        'pending'   | [TaskStatus.NEW]
        'succeeded' | [TaskStatus.COMPLETED]
        'fail'      | [TaskStatus.FAILED]
        'abort'     | [TaskStatus.ABORTED]
        '*ing'      | [TaskStatus.NEW, TaskStatus.RUNNING] // matches 'pending' (ie. NEW) and 'running'
    }

}

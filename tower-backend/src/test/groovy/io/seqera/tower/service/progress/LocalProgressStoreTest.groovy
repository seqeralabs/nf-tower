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

package io.seqera.tower.service.progress

import java.time.Duration

import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.service.progress.LocalStatsStore
import io.seqera.tower.service.progress.ProgressState
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class LocalProgressStoreTest extends Specification {

    def 'should store counters' () {

        given:
        def store = new LocalStatsStore()

        when:
        store.storeProgress('abc', new ProgressState('abc', ['foo', 'bar']))
        then:
        store.getProgress('abc') == new ProgressState('abc', ['foo', 'bar'])

        expect:
        store.findExpiredKeys( Duration.parse('PT1m') ) == []
        store.findExpiredKeys( Duration.parse('PT0m') ) == ['abc']

    }

    def 'should update status' () {
        given:
        def store = new LocalStatsStore()

        when:
        store.storeProgress('abc', new ProgressState('abc', ['foo', 'bar']))
        then:
        store.getTaskStatus('abc', 1 )== null

        when:
        store.storeTaskStatuses('abc', [1: TaskStatus.SUBMITTED, 2: TaskStatus.COMPLETED])
        then:
        store.getTaskStatus('abc', 1) ==TaskStatus.SUBMITTED
        store.getTaskStatus('abc', 2) ==TaskStatus.COMPLETED
        store.getTaskStatus('abc', 3) ==null
        store.getTaskStatus('xyz', 1) ==null

        when:
        store.storeTaskStatuses('abc', [1: TaskStatus.SUBMITTED, 2: null])
        then:
        store.getTaskStatus('abc', 1) ==TaskStatus.SUBMITTED
        store.getTaskStatus('abc', 2) ==null
        !store.taskStatus.get('abc').containsKey(2)
    }

}

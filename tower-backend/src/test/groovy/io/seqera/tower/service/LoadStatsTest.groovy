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

package io.seqera.tower.service

import java.time.Duration
import java.time.Instant

import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class LoadStatsTest extends Specification {

    def 'should validate olderThan' () {

        given:
        final duration = Duration.parse('PT0.2s')

        expect:
        duration.toMillis() == 200
        and:
        final stats = new LoadStats(timestamp: Instant.now())
        !stats.olderThan( duration )
        and:
        // wait 200 ms
        sleep 300
        and:
        // now the stats is older then 100ms
        stats.olderThan( duration )

    }

    def 'should update load stats' () {
        given:
        def stats0 = new LoadStats('xyz')

        when:
        def stats1 = stats0.update([1L: new TaskLoad(cpus:1, memory: 2)], [])
        then:
        stats1.loadTasks == 1
        stats1.loadCpus == 1
        stats1.loadMemory == 2
        and:
        stats1.peakTasks == 1
        stats1.peakCpus == 1
        stats1.peakMemory == 2
        and:
        stats1.load == [
                1L: new TaskLoad(cpus:1, memory: 2)
        ]

        when:
        def stats2 = stats1.update([2L: new TaskLoad(cpus:10, memory: 20)], [])
        then:
        stats2.loadTasks == 2
        stats2.loadCpus == 11
        stats2.loadMemory == 22
        and:
        stats2.peakTasks == 2
        stats2.peakCpus == 11
        stats2.peakMemory == 22
        and:
        stats2.load == [
                1L: new TaskLoad(cpus:1, memory: 2),
                2L: new TaskLoad(cpus:10, memory: 20)
        ]

        when:
        def stats3 = stats2.update([3L: new TaskLoad(cpus:3, memory: 4)], [1L,2L])
        then:
        stats3.loadTasks == 1
        stats3.loadCpus == 3
        stats3.loadMemory == 4
        and:
        stats3.peakTasks == 2
        stats3.peakCpus == 11
        stats3.peakMemory == 22
        and:
        stats3.load == [
                3L: new TaskLoad(cpus:3, memory: 4)
        ]
    }
}

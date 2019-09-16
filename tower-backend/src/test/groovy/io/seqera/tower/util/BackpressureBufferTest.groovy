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

package io.seqera.tower.util

import java.time.Duration

import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class BackpressureBufferTest extends Specification{

    def 'should check defaults' () {
        when:
        def buffer = new BackpressureBuffer()
        then:
        buffer.maxCount == 100 
        buffer.heartbeat.toMillis() == 5 * 60 * 1_000
        buffer.timeout.toMillis() == 1_000
    }

    def 'should buffer events burst' () {

        given:
        def events = []
        def buffer = new BackpressureBuffer()
                    .setTimeout(Duration.parse('PT0.1s'))
                    .setHeartbeat(Duration.parse('PT0.5s'))
                    .onNext { events << it }
                    .start()

        assert buffer.timeout.toMillis() == 100
        assert buffer.heartbeat.toMillis() == 500

        when:
        for( int it=1; it<=10; it++ )
            buffer.offer(it.intdiv(2))
        then:
        !events

        when:
        sleep 150
        then:
        events.size()==1
        events[0] == [1,2,3,4,5]

        when:
        buffer.offer('hello')
        sleep 100
        then:
        events.size()==2
        events[1] == ['hello']

        when:
        sleep(600)
        then:
        events.size()==3
        events == [[1, 2, 3, 4, 5], ['hello'], []]

        when:
        sleep(2_000)
        println events
        then:
        events == [[1, 2, 3, 4, 5], ['hello'], [], [], [], [], []]

        when:
        buffer.terminateAndAwait()
        then:
        noExceptionThrown()
    }


}

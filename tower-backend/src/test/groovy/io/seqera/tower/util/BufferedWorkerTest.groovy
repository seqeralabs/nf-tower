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

import java.util.concurrent.Executors

import groovy.util.logging.Slf4j
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
class BufferedWorkerTest extends Specification {

    def 'should invoke buffer event' () {

        given:
        def debounce = new BufferedWorker()
        log.info "instance = ${this}"
        def events = []

        when:
        debounce
                .withBuffer( 3)
                .onData { events << it }
        and:
        5.times { debounce.publish(it+1) }
        and:
        debounce.awaitCompletion()
        
        then:
        events == [ [1,2,3], [4,5] ]
    }

    def 'should invoke buffer event with timeout' () {

        given:
        def debounce = new BufferedWorker()
        def events = []

        when:
        debounce
                .withBuffer( 3, 100)
                .onData { events << it }
        and:
        5.times { debounce.publish(it+1) }
        and:
        debounce.awaitCompletion()

        then:
        events == [ [1,2,3], [4,5] ]
    }

    def 'should invoke buffer event with scheduler' () {

        given:
        def debounce = new BufferedWorker()
        def events = new ArrayList()

        when:
        debounce
                .withExecutor(Executors.newCachedThreadPool(),true)
                .withBuffer(3, 100)
                .onData {  log.info("$it");  events << it }
        and:
        5.times { debounce.publish(it+1) }
        and:
        debounce.awaitCompletion()

        then:
        events.size() ==2 
        events.contains([1,2,3])
        events.contains([4,5])
    }


    def 'should not break worker' () {

        given:
        def debounce = new BufferedWorker()
        def events = []

        when:
        def count=0
        debounce
                .withBuffer(3)
                .onData { if(count++==1) throw new Exception("Oops")  else   events << it }
        and:
        9.times { debounce.publish(it+1) }
        and:
        debounce.awaitCompletion()

        then:
        // the second entry `4,5,6` is lost
        events == [ [1,2,3], [7,8,9] ]
    }

    def 'should retry on error' () {

        given:
        def debounce = new BufferedWorker()
        def events = []

        when:
        def count=0
        debounce
                .withAttempts(1)
                .withBuffer(3)
                .onData { if(count++ % 2 == 0) throw new Exception("Oops with $it")  else   events << it }
        and:
        9.times { debounce.publish(it+1) }
        and:
        debounce.awaitCompletion()

        then:
        events == [ [1,2,3], [4,5,6], [7,8,9] ]
    }

    def 'should call failure handler' () {

        given:
        def debounce = new BufferedWorker()
        def events = []

        when:
        def error=null
        debounce
                .withAttempts(2)
                .withBuffer(3)
                .onFailure { error = it }
                .onData{ if(it==[1,2,3]) throw new Exception("Oops with $it")  else   events << it }
        and:
        9.times { debounce.publish(it+1) }
        and:
        debounce.awaitCompletion()

        then:
        events == [ [4,5,6], [7,8,9] ]
        and:
        error.message == "Oops with [1, 2, 3]"
    }



}

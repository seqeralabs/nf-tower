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

import io.reactivex.subjects.PublishSubject
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class RxHelperTest extends Specification {

    def 'should await completion' () {
        given:
        def rnd = new Random()
        def subject = PublishSubject.create()

        def test = subject.test()

        when:
        subject.subscribe( { sleep( rnd.nextInt(100) ) } )
        Thread.start {
            50.times { subject.onNext(it) }
            subject.onComplete()
        }
        RxHelper.await(subject)
        
        then:
        test.valueCount() == 50
    }

    def 'should exit on error' () {
        given:
        def subject = PublishSubject.create()

        when:
        subject.subscribe( { throw new IllegalStateException("Ouch!")  }, { println "Error captured: $it" } )
        subject.onNext('Hello')
        subject.onComplete()
        RxHelper.await(subject)

        then:
        noExceptionThrown()
    }

    def 'should not hang' () {
        given:
        def subject = PublishSubject.create()

        when:
        subject.subscribe( { sleep 100 } )
        subject.onNext(1)
        subject.onComplete()
        sleep 100
        and:
        RxHelper.await(subject)
        then:
        noExceptionThrown()

    }
}

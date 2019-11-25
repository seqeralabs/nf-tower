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


import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ProgressStateTest extends Specification {

    def 'should return not null entries' () {
        given:
        def state = new ProgressState('xyz-123', ['foo','bar'])

        when:
        def list = state.getProcessLoads()
        then:
        list *. process == ['foo','bar']
    }

    def 'should return a named load' () {
        when:
        def state = new ProgressState('xyz-123', ['foo','bar'])
        then:
        state.getState('foo').process == 'foo'
        state.getState('bar').process == 'bar'
        state.getState('gamma').process == 'gamma'
    }

    def 'should be equals' () {
        when:
        def s1 = new ProgressState('abc', ['foo','bar'])
        def s2 = new ProgressState('abc', ['foo','bar'])
        def s3 = new ProgressState('abc', ['alpha','beta'])
        then:
        s1 == s2
        s1 != s3
    }


}

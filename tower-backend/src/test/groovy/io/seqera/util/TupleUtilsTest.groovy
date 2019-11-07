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

package io.seqera.util

import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class TupleUtilsTest extends Specification {

    def 'should create a list' () {
        expect:
        TupleUtils.single(1) == [1]
        TupleUtils.pair(1,2) == [1,2]
        TupleUtils.triple(1,2, 3) == [1,2,3]
    }

    def 'should create a map' () {
        expect:
        TupleUtils.map('x',1) == [x:1]
        TupleUtils.map('x',1, 'y',2) == [x:1,y:2]
        TupleUtils.map('x',1, 'y',2, 'z', 3) == [x:1,y:2,z:3]
    }

    def 'should create a set' () {
        expect:
        TupleUtils.set(1) == [1] as Set
        TupleUtils.set(1,2) == [1,2] as Set
        TupleUtils.set(1,2,3) == [1,2,3] as Set
    }

}

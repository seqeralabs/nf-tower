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
class CheckHelperTest extends Specification {

    def 'test is valid' () {

        expect:
        CheckHelper.isValid( 1, Integer )
        CheckHelper.isValid( 1, [1, 2, 3] )
        CheckHelper.isValid( 1, 1 )
        CheckHelper.isValid( 10, ~/\d+/ )
        !CheckHelper.isValid( 'abc', ~/\d+/ )

        CheckHelper.isValid( 'abc', ~/a*b*c/ )
        !CheckHelper.isValid( 'abz', ~/a*b*c/ )

        !CheckHelper.isValid( 1, [2, 3] )
        !CheckHelper.isValid( 1, String )
        !CheckHelper.isValid( 1, 3 )
        !CheckHelper.isValid( 1, null )
        CheckHelper.isValid( null, null )

    }


    def 'test checkParams with map' () {

        when:
        CheckHelper.checkParams('hola', [x:1], [x:[1, 2, 3]] )
        then:
        true

        when:
        CheckHelper.checkParams('hola', [x:1], [x:[2, 3]] )
        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Value '1' cannot be used in in parameter 'x' for operator 'hola' -- Possible values: 2, 3"

        when:
        CheckHelper.checkParams('hola', [x:1], [x:Integer] )
        then:
        true

        when:
        CheckHelper.checkParams('hola', [x:1], [x:String] )
        then:
        e = thrown(IllegalArgumentException)
        e.message == "Value '1' cannot be used in in parameter 'x' for operator 'hola' -- Value don't match: class java.lang.String"

        when:
        CheckHelper.checkParams('hola', [x:true], [x:[Boolean, [1, 2, 3]]] )
        then:
        true

        when:
        CheckHelper.checkParams('hola', [x:2], [x:[Boolean, [1, 2, 3]]] )
        then:
        true

        when:
        CheckHelper.checkParams('hola', [x:'Ciao'], [x:[Boolean, [1, 2, 3]]] )
        then:
        e = thrown(IllegalArgumentException)
        e.message == "Value 'Ciao' cannot be used in in parameter 'x' for operator 'hola' -- Possible values: class java.lang.Boolean, [1, 2, 3]"

    }

    def 'test checkParams splitter map' () {

        def valid = [
                each: Closure,
                by: Integer,
                into: [ Collection ],
                record: [ Boolean, Map ],
                autoClose: Boolean,
                meta: ['file','path','index']
        ]

        when:
        CheckHelper.checkParams ('splitter', [into: [], count: 2], valid)
        then:
        thrown(IllegalArgumentException)

    }


    def 'test checkParams with list' () {

        when:
        CheckHelper.checkParams('hola', [x:1], 'x', 'y' )
        CheckHelper.checkParams('hola', [x:1, y:2], 'x', 'y' )
        then:
        true

        when:
        CheckHelper.checkParams('hola', [z:1], 'x', 'y' )
        then:
        thrown(IllegalArgumentException)

    }

    def 'should return the nearest match' () {

        given:
        def salut = ['hello','hola','halo','hey','ciao','bonjour']

        expect:
        CheckHelper.closest(salut, 'hola') == ['hola']
        CheckHelper.closest(salut,'hol') == ['hola']
        CheckHelper.closest(salut, 'cioa') == ['ciao']
        CheckHelper.closest(salut, 'helo') == ['hello', 'halo']
    }

}

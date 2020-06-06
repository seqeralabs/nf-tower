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
import spock.lang.Unroll
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class StringUtilsTest extends Specification {

    @Unroll
    def 'should check string like' () {

        expect:
        StringUtils.like(STR,LIKE) == EXPECT

        where:
        STR                 | LIKE          | EXPECT
        'foo'               | 'foo'         | true
        'foo'               | 'f*'          | true
        'foo'               | '*'           | true
        'foo'               | 'bar'         | false
        'foo'               | '*ar'         | false
        'paolo@gmail.com'   | '*@gmail.com' | true
        'PAOLO@gmail.com'   | '*@gmail.com' | true
        'paolo@yahoo.com'   | '*@gmail.com' | false
        'x.y_w-z@this.com'  | '*@this.com'  | true
        'x.y_w-z@THIS.com'  | '*@this.com'  | true
        'x.y_w-z@that.com'  | '*@this.com'  | false
    }


    def 'should find similar strings' () {

        expect:
        StringUtils.findSimilar(ITEMS, STR) == EXPECTED

        where:
        STR         | ITEMS                 | EXPECTED
        'foo'       | []                    | []
        'foa'       | ['foo', 'bar']        | ['foo']
        'foa'       | ['foo', 'bar']        | ['foo']
        'helo'      | ['Hola', 'Hello', 'Ciao']  | ['Hello']

    }

    @Unroll
    def 'should get url protocol' () {
        expect:
        StringUtils.getUrlProtocol(STR)  == EXPECTED
        where:
        EXPECTED    | STR
        'ftp'       | 'ftp://abc.com'
        's3'        | 's3://bucket/abc'
        null        | '3s://bucket/abc'
        null        | 'abc:xyz'
        null        | '/a/bc/'
    }

    @Unroll
    def 'should validate sha1 string' () {
        expect:
        StringUtils.isSha1String(STR) == EXPECTED
        where:
        STR             | EXPECTED
        null            | false
        ''              | false
        '12bc'          | false
        '7299bc8a'      | true
        '4c5b348c'      | true
        'foo'           | false
        'hello_world'   | false

    }
}

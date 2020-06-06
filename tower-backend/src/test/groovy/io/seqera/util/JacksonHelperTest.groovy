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
class JacksonHelperTest extends Specification {

    def 'should ser an object' () {

        given:
        def EXPECT = '{"foo":1,"bar":"dos","baz":true}'
        def obj = [foo: 1, bar: 'dos', baz: true]

        expect:
        JacksonHelper.toJson(obj) == EXPECT
        JacksonHelper.toJson(null) == null
    }

    def 'should deser an object' () {
        given:
        def JSON = '{"foo":1,"bar":"dos","baz":true}'
        def EXPECT = [foo: 1, bar: 'dos', baz: true]

        expect:
        JacksonHelper.fromJson(JSON, Map) == EXPECT
        JacksonHelper.fromJson(null, Map) == null

    }

}

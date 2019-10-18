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

}

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
class TokenHelperTest extends Specification {

    def 'should create random token' () {
        when:
        def tkn1 = TokenHelper.createHexToken()
        def tkn2 = TokenHelper.createHexToken()
        then:
        tkn1.length() == 40
        tkn2.length() == 40
        tkn1 != tkn2
    }

}


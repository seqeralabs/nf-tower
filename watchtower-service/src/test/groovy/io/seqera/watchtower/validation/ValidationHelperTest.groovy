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

package io.seqera.watchtower.validation

import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ValidationHelperTest extends Specification {

    def 'should check validation code strings' () {

        expect:
        CODE.toString() == EXPECTED
        
        where:
        CODE                                | EXPECTED
        ValidationHelper.Code.nullable      | 'nullable'
        ValidationHelper.Code.unique        | 'unique'
        ValidationHelper.Code.invalid       | 'invalid'
        ValidationHelper.Code.email_invalid | 'email.invalid'
        ValidationHelper.Code.url_invalid   | 'url.invalid'
    }

}

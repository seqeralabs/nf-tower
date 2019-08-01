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

package io.seqera.tower.service

import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class UserServiceImplTest extends Specification {

    def 'should make a user name from email' () {
        given:
        UserServiceImpl service = new UserServiceImpl()

        expect:
        service.makeUserNameFromEmail(EMAIL) == EXPECTED

        where:
        EXPECTED    | EMAIL
        'foo'       | 'foo@bar.com'
        'f00'       | 'f00@bar.com'
        'foo-x'     | 'foo.x@bar.com'
        'foo-x'     | 'foo......x@bar.com'
        'foo'       | 'foo......@bar.com'
        'foo'       | '......foo@bar.com'
        ''          | '......@bar.com'
        'x-y-z'     | 'x...y..z--@bar.com'
    }

    def 'validate trusted email ' () {

        when:
        def service = new UserServiceImpl()
        then:
        !service.isTrustedEmail('me@foo.com')

        when:
        service = new UserServiceImpl(trustedEmails: ['*@foo.com'])
        then:
        service.isTrustedEmail('me@foo.com')
        !service.isTrustedEmail('you@bar.com')

    }
}

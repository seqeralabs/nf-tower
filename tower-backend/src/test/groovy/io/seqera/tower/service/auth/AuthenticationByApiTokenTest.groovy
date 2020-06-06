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

package io.seqera.tower.service.auth

import javax.inject.Inject
import java.time.Instant

import io.micronaut.security.authentication.UserDetails
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.seqera.tower.domain.AccessToken
import io.seqera.tower.domain.User
import io.seqera.tower.service.AccessTokenService
import io.seqera.tower.service.UserService
import io.seqera.tower.service.UserServiceImpl
import io.seqera.tower.service.mail.MailService
import io.seqera.util.TokenHelper
import spock.lang.Specification

@MicronautTest
class AuthenticationByApiTokenTest extends Specification {

    @Inject
    AuthenticationByApiToken authProvider

    @Inject
    UserService userService

    @MockBean(MailService)
    MailService mockMailService() {
        GroovyMock(MailService)
    }

    @MockBean(UserServiceImpl)
    UserService mockUserService() {
        Mock(UserServiceImpl)
    }

    def 'should allow access new user' () {
        given:
        def email = 'user@seqera.io'
        def now = Instant.now()
        def TOKEN = TokenHelper.createHexToken()
        and:
        def USER = new User(id:100, email:email, userName: email, authToken: TOKEN, authTime: now, accessTokens: [new AccessToken(token: 'token')])
        def ROLES = ['ROLE_X']

        when:
        def result = authProvider.authToken0(TOKEN)

        then:
        userService.getByAccessToken(TOKEN) >> USER
        userService.findAuthoritiesByUser(USER) >> ROLES

        and:
        result instanceof UserDetails
        with( (UserDetails) result ) {
            username == USER.getUid()
            roles == ROLES
        }
    }

    def 'should auth with user name' () {
        given:
        def NAME = 'the_user_name'
        def EMAIL = 'foo@gmail'
        def TOKEN = 'xyz'
        def USER = new User(id: 100, userName: NAME, email:EMAIL)
        def userService = Mock(UserService)
        def tokenService = Mock(AccessTokenService)
        def provider = Spy(AuthenticationByApiToken)
        provider.userService = userService
        provider.tokenService = tokenService

        when:
        def result = provider.authToken0(TOKEN)
        then:
        1 * userService.getByAccessToken(TOKEN) >> USER
        1 * userService.findAuthoritiesByUser(USER) >> ['role_a', 'role_b']
        1 * tokenService.updateLastUsedAsync(TOKEN)
        then:
        result instanceof UserDetails
        (result as UserDetails).username == USER.getUid()
        (result as UserDetails).roles == ['role_a', 'role_b']

    }


}

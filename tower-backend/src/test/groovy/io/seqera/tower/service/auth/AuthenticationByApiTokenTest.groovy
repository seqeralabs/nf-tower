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

import io.micronaut.security.authentication.UserDetails
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.seqera.tower.service.AccessTokenService
import io.seqera.util.TokenHelper
import io.seqera.tower.domain.AccessToken
import io.seqera.tower.domain.User
import io.seqera.tower.service.mail.MailService
import io.seqera.tower.service.UserService
import io.seqera.tower.service.UserServiceImpl
import spock.lang.Specification

import javax.inject.Inject
import java.time.Instant

@MicronautTest
class AuthenticationByApiTokenTest extends Specification {

    @Inject
    AuthenticationByApiToken authProvider

    @Inject
    UserService userService

    def 'should allow access new user' () {
        given: "register a user"
        User user = userService.access('user@seqera.io')

        when:
        def result = authProvider.authToken0(user.accessTokens.first().token)

        then:
        result instanceof UserDetails
        result.username == 'foo'
    }

    def 'should auth with user name' () {
        given:
        def NAME = 'the_user_name'
        def EMAIL = 'foo@gmail'
        def TOKEN = 'xyz'
        def USER = new User(userName: NAME, email:EMAIL)
        def userService = Mock(UserService)
        def tokenService = Mock(AccessTokenService)
        def provider = Spy(AuthenticationByApiToken)
        provider.userService = userService
        provider.tokenService = tokenService

        when:
        def result = provider.authToken0(TOKEN)
        then:
        1 * userService.getByAccessToken(TOKEN) >> USER
        1 * userService.findAuthoritiesOfUser(USER) >> ['role_a', 'role_b']
        1 * tokenService.updateLastUsedAsync(TOKEN)
        then:
        result instanceof UserDetails
        (result as UserDetails).username == EMAIL
        (result as UserDetails).roles == ['role_a', 'role_b']

    }

    @MockBean(MailService)
    MailService mockMailService() {
        GroovyMock(MailService)
    }

    @MockBean(UserServiceImpl)
    UserService mockUserService() {
        final now = Instant.now()
        final tkn = TokenHelper.createHexToken()
        GroovyMock(UserServiceImpl) {
            access(_ as String) >> { String email -> new User(email:email, userName: email, authToken: tkn, authTime: now, accessTokens: [new AccessToken(token: 'token')]) }
            getByAccessToken(_ as String) >> { args -> new User(email:'foo', userName:'foo', authToken: args[0], authTime: now) }
            findAuthoritiesOfUser(_ as User) >> ['role_a', 'role_b']
        }
    }
}

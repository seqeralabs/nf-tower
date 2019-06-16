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

package io.seqera.watchtower.service.auth

import io.micronaut.security.authentication.UserDetails
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.seqera.util.TokenHelper
import io.seqera.watchtower.domain.AccessToken
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.service.MailService
import io.seqera.watchtower.service.MailServiceImpl
import io.seqera.watchtower.service.UserService
import io.seqera.watchtower.service.UserServiceImpl
import spock.lang.Specification

import javax.inject.Inject
import java.time.Instant

@MicronautTest
class AuthenticationProviderByAccessTokenTest extends Specification {

    @Inject
    AuthenticationProviderByAccessToken authProvider

    @Inject
    UserService userService

    def 'should allow access new user' () {
        given: "register a user"
        User user = userService.register('user@seqera.io')

        when:
        def result = authProvider.authenticate0(user.userName, user.accessTokens.first().token)

        then:
        result instanceof UserDetails
        result.username == 'user@seqera.io'
    }

    def 'should auth with user name' () {
        given:
        def NAME = 'the_user_name'
        def EMAIL = 'foo@gmail'
        def TOKEN = 'xyz'
        def USER = new User(userName: NAME, email:EMAIL)
        def userService = Mock(UserService)
        AuthenticationProviderByAccessToken provider = Spy(AuthenticationProviderByAccessToken, constructorArgs: [userService])

        when:
        def result = provider.authenticate0(NAME, TOKEN)
        then:
        1 * userService.findByUserNameAndAccessToken (NAME,TOKEN) >> USER
        1 * userService.findAuthoritiesOfUser(USER) >> ['role_a', 'role_b']
        then:
        result instanceof UserDetails
        (result as UserDetails).username == EMAIL
        (result as UserDetails).roles == ['role_a', 'role_b']

    }

    @MockBean(MailServiceImpl)
    MailService mockMailService() {
        GroovyMock(MailService)
    }

    @MockBean(UserServiceImpl)
    UserService mockUserService() {
        final now = Instant.now()
        final tkn = TokenHelper.createHexToken()
        GroovyMock(UserServiceImpl) {
            register(_ as String) >> { String email -> new User(email:email, userName: email, authToken: tkn, authTime: now, accessTokens: [new AccessToken(token: 'token')]) }
            findByUserNameAndAccessToken(_ as String, _ as String) >> { args -> new User(email:args[0], userName:args[0], authToken: args[1], authTime: now) }
            findAuthoritiesOfUser(_ as User) >> ['role_a', 'role_b']
        }
    }
}

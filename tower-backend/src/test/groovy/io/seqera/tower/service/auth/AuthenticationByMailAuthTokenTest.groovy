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
import java.time.Duration
import java.time.Instant

import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.UserDetails
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.seqera.tower.domain.User
import io.seqera.tower.service.UserService
import io.seqera.tower.service.UserServiceImpl
import io.seqera.tower.service.mail.MailService
import io.seqera.util.TokenHelper
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class AuthenticationByMailAuthTokenTest extends Specification {

    @Inject
    AuthenticationByMailAuthToken authProvider

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

    def 'should inject auth duration property' () {
        expect:
        authProvider.authMailDuration == Duration.ofMinutes(30)
    }


    def 'should allow access new user' () {
        given: 
        def email = 'user@seqera.io'
        def now = Instant.now()
        def tkn = TokenHelper.createHexToken()
        def USER = new User(id: 100, email:email, userName: email, authToken: tkn, authTime: now)
        def ROLES = ['role_x', 'role_z']

        when:
        def result = authProvider.authenticate0(USER.getUid(), USER.authToken)

        then:
        userService.findByUidAndAuthToken(USER.getUid(), USER.authToken) >> USER
        userService.findAuthoritiesByUser(USER) >> ROLES

        and:
        result instanceof UserDetails
        with( (UserDetails) result ) {
            username == USER.getUid()
            roles == ROLES
        }
    }

    def 'should reject user access' () {
        given:
        def email = 'user@seqera.io'
        def now = Instant.now()
        def tkn = TokenHelper.createHexToken()
        def USER = new User(id: 100, email:email, userName: email, authToken: tkn, authTime: now)
        def ROLES = ['role_x', 'role_z']

        when:
        authProvider.authMailDuration = Duration.ofMillis(100)
        sleep 200 // <-- wait longer than expected token duration

        def result = authProvider.authenticate0(USER.getUid(), USER.authToken)

        then:
        result instanceof AuthenticationFailed
    }


    def 'should auth with email' () {
        given:
        def EMAIL = 'foo@gmail.com'
        def TOKEN = 'xyz'
        def USER = new User(id: 100, email: EMAIL)
        def UID = USER.getUid()
        def userService = Mock(UserService)
        AuthenticationByMailAuthToken provider = Spy(AuthenticationByMailAuthToken, constructorArgs: [userService])

        when:
        def result = provider.authenticate0(UID, TOKEN)
        then:
        1 * userService.findByUidAndAuthToken (UID,TOKEN) >> USER
        1 * provider.isAuthTokenExpired(USER) >> false
        1 * userService.findAuthoritiesByUser(USER) >> ['role_x']
        then:
        result instanceof UserDetails
        (result as UserDetails).username == UID
        (result as UserDetails).roles == ['role_x']

        when:
        result = provider.authenticate0(UID, TOKEN)
        then:
        1 * userService.findByUidAndAuthToken (UID,TOKEN) >> USER
        1 * provider.isAuthTokenExpired(USER) >> true
        0 * userService.findAuthoritiesByUser(USER) >> null
        then:
        result instanceof AuthFailure
        (result as AuthFailure).message.get() == ("Authentication token expired for user: $UID")

    }


}

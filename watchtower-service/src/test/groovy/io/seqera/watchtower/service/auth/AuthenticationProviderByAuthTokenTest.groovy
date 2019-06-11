package io.seqera.watchtower.service.auth

import javax.inject.Inject
import java.time.Duration
import java.time.Instant

import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.UserDetails
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.seqera.util.TokenHelper
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.service.MailService
import io.seqera.watchtower.service.MailServiceImpl
import io.seqera.watchtower.service.UserService
import io.seqera.watchtower.service.UserServiceImpl
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest
class AuthenticationProviderByAuthTokenTest extends Specification {

    @Inject
    AuthenticationProviderByAuthToken authProvider

    @Inject
    UserService userService

    def 'should inject auth duration property' () {
        expect:
        authProvider.authMailDuration == Duration.ofMinutes(30)
    }


    def 'should allow access new user' () {
        given: "register a user"
        User user = userService.register('user@seqera.io')

        when:
        def result = authProvider.authenticate0(user.email, user.authToken)

        then:
        result instanceof UserDetails
        result.username == 'user@seqera.io'
    }

    def 'should reject user access' () {
        given: "register a user"
        User user = userService.register('user@seqera.io')

        when:
        authProvider.authMailDuration = Duration.ofMillis(100)
        sleep 200 // <-- wait longer than expected token duration

        def result = authProvider.authenticate0(user.email, user.authToken)

        then: "it should return a failure because the token expired"
        result instanceof AuthenticationFailed
    }


    def 'should auth with email' () {
        given:
        def EMAIL = 'foo@gmail.com'
        def TOKEN = 'xyz'
        def USER = new User(email: EMAIL)
        def userService = Mock(UserService)
        AuthenticationProviderByAuthToken provider = Spy(AuthenticationProviderByAuthToken, constructorArgs: [userService])

        when:
        def result = provider.authenticate0(EMAIL, TOKEN)
        then:
        1 * userService.findByEmailAndAuthToken (EMAIL,TOKEN) >> USER
        1 * provider.isAuthTokenExpired(USER) >> false
        1 * userService.findAuthoritiesByEmail(EMAIL) >> ['role_x']
        then:
        result instanceof UserDetails
        (result as UserDetails).username == EMAIL
        (result as UserDetails).roles == ['role_x']

        when:
        result = provider.authenticate0(EMAIL, TOKEN)
        then:
        1 * userService.findByEmailAndAuthToken (EMAIL,TOKEN) >> USER
        1 * provider.isAuthTokenExpired(USER) >> true
        0 * userService.findAuthoritiesByEmail(EMAIL) >> null
        then:
        result instanceof AuthFailure
        (result as AuthFailure).message.get() == ("Authentication token expired for user: $EMAIL")

    }

    def 'should auth with user name' () {
        given:
        def NAME = 'the_user_name'
        def EMAIL = 'foo@gmail'
        def TOKEN = 'xyz'
        def USER = new User(userName: NAME, email:EMAIL)
        def userService = Mock(UserService)
        AuthenticationProviderByAuthToken provider = Spy(AuthenticationProviderByAuthToken, constructorArgs: [userService])

        when:
        def result = provider.authenticate0(NAME, TOKEN)
        then:
        1 * userService.findByUserNameAndAccessToken (NAME,TOKEN) >> USER
        1 * userService.findAuthoritiesByEmail(EMAIL) >> ['role_x']
        then:
        result instanceof UserDetails
        (result as UserDetails).username == EMAIL
        (result as UserDetails).roles == ['role_x']

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
            register(_ as String) >> { String email -> new User(email:email, userName: email, authToken: tkn, authTime: now) }
            findByEmailAndAuthToken(_ as String, _ as String) >> { args -> new User(email:args[0], userName:args[0], authToken: args[1], authTime: now) }
            findAuthoritiesByEmail(_ as String) >> ['role_a', 'role_b']
        }
    }

}

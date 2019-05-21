package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.auth.User
import io.seqera.watchtower.domain.auth.UserRole
import io.seqera.watchtower.service.auth.UserService
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator

import javax.inject.Inject

@MicronautTest(application = Application.class)
@Transactional
class UserServiceTest extends AbstractContainerBaseTest {

    @Inject
    UserService userService


    void "register a new user"() {
        given: "an email"
        String email = 'user@seqera.io'

        when: "register the user"
        User user = userService.register(email)

        then: "the user has been created"
        user.id
        user.email == email
        user.username == email.replaceAll(/@.*/, '')
        user.authToken
        User.count() == 1

        and: "a role was attached to the user"
        UserRole.first().user.id == user.id
        UserRole.first().role.authority == 'ROLE_USER'
    }

    void "register a user already registered"() {
        given: "an existing user"
        User existingUser = new DomainCreator().createUser()

        when: "register a user with the same email of the previous one"
        User userToRegister = userService.register(existingUser.email)

        then: "the returned user is the same as the previous one"
        existingUser.id == userToRegister.id
        existingUser.email == userToRegister.email
        existingUser.authToken == userToRegister.authToken
        User.count() == 1
    }

    void "try to register a user given an invalid email"() {
        given: "an invalid email"
        String badEmail = 'badEmail'

        when: "register a user with a bad email"
        User user = userService.register(badEmail)

        then: "the user couldn't be created"
        user.hasErrors()
        user.errors.getFieldError('email')
        User.count() == 0
    }

}

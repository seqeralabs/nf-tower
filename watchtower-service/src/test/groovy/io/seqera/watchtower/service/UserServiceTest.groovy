package io.seqera.watchtower.service

import grails.gorm.transactions.Transactional
import io.micronaut.security.authentication.DefaultAuthentication
import io.micronaut.test.annotation.MicronautTest
import io.seqera.mail.MailerConfig
import io.seqera.watchtower.Application
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.domain.UserRole
import io.seqera.watchtower.pogo.exceptions.NonExistingUserException
import io.seqera.watchtower.util.AbstractContainerBaseTest
import io.seqera.watchtower.util.DomainCreator
import org.subethamail.wiser.Wiser

import javax.inject.Inject
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart
import javax.validation.Validation
import javax.validation.ValidationException
import java.security.Principal

@MicronautTest(application = Application.class)
@Transactional
class UserServiceTest extends AbstractContainerBaseTest {

    @Inject
    UserService userService

    @Inject
    MailerConfig mailerConfig

    Wiser smtpServer

    void setup() {
        smtpServer = new Wiser(mailerConfig.smtp.port)
        smtpServer.start()
    }

    void cleanup() {
        smtpServer.stop()
    }

    void "register a new user"() {
        given: "an email"
        String email = 'user@seqera.io'

        when: "register the user"
        User user = userService.register(email)

        then: "the user has been created"
        user.id
        user.email == email
        user.userName == email.replaceAll(/@.*/, '')
        user.authToken
        User.count() == 1

        and: "a role was attached to the user"
        UserRole.first().user.id == user.id
        UserRole.first().role.authority == 'ROLE_USER'

        and: "the access link was sent to the user"
        smtpServer.messages.size() == 1
        Message message = smtpServer.messages.first().mimeMessage
        message.allRecipients.contains(new InternetAddress(user.email))
        message.subject == 'NF-Tower Sign in'
        (message.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains('Welcome in NF-Tower!')
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

        and: 'the access email has been sent'
        smtpServer.messages.size() == 1
        Message message = smtpServer.messages.first().mimeMessage
        message.allRecipients.contains(new InternetAddress(existingUser.email))
        message.subject == 'NF-Tower Sign in'
        (message.content as MimeMultipart).getBodyPart(0).content.getBodyPart(0).content.contains('Welcome in NF-Tower!')
    }

    void "try to register a user given an invalid email"() {
        given: "an invalid email"
        String badEmail = 'badEmail'

        when: "register a user with a bad email"
        userService.register(badEmail)

        then: "the user couldn't be created"
        ValidationException e = thrown(ValidationException)
        e.message == "Can't save a user with bad email format"
        User.count() == 0
    }

    void "update an existing user given new user data"() {
        given: 'an existing user'
        User user = new DomainCreator().createUser()

        and: 'some new data encapsulated in a user object'
        User userData = new DomainCreator(save: false).createUser(userName: 'user', firstName: 'User', lastName: 'Userson', avatar: 'https://i.pravatar.cc/200', organization: 'Org', description: 'Desc')

        when: 'update the user'
        User updatedUser = userService.update(new DefaultAuthentication(user.email, null), userData)

        then: "the user has been correctly updated"
        updatedUser.userName == userData.userName
        updatedUser.firstName == userData.firstName
        updatedUser.lastName == userData.lastName
        updatedUser.avatar == userData.avatar
        updatedUser.organization == userData.organization
        updatedUser.description == userData.description
        User.count() == 1
    }

    void "try to update an existing user, but with some invalid data"() {
        given: 'an existing user'
        User user = new DomainCreator().createUser()

        and: 'some new data encapsulated in a user object'
        User userData = new DomainCreator(save: false).createUser(avatar: 'badUrl')

        when: 'update the user'
        User updatedUser = userService.update(new DefaultAuthentication(user.email, null), userData)

        then: "a validation exception is thrown"
        ValidationException e = thrown(ValidationException)
        e.message == "Can't save a user with bad avatar URL format"
    }

    void "try to update a non existing user"() {
        given: 'some new data encapsulated in a user object'
        User userData = new DomainCreator(save: false).createUser(avatar: 'badUrl')

        when: 'update a non existing user'
        User updatedUser = userService.update(new DefaultAuthentication('nonexistinguser@email.com', null), userData)

        then: "a non-existing exception is thrown"
        NonExistingUserException e = thrown(NonExistingUserException)
        e.message == "The user to update doesn't exist"
    }

}

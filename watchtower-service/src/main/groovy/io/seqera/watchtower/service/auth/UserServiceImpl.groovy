package io.seqera.watchtower.service.auth

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.micronaut.context.annotation.Value
import io.seqera.mail.Mail
import io.seqera.watchtower.domain.auth.Role
import io.seqera.watchtower.domain.auth.User
import io.seqera.watchtower.domain.auth.UserRole
import io.seqera.watchtower.service.MailService
import io.seqera.watchtower.service.MailServiceImpl
import io.seqera.watchtower.service.WorkflowService
import org.springframework.validation.FieldError

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException

@Singleton
@Transactional
class UserServiceImpl implements UserService {


    @Value('front.url')
    String frontendUrl

    MailService mailService

    @Inject
    UserServiceImpl(MailService mailService) {
        this.mailService = mailService
    }


    @CompileDynamic
    User register(String email) {
        User existingUser = User.findByEmail(email)

        if (existingUser) {
            return existingUser
        }

        User user = createUser(email, 'ROLE_USER')
        checkUserSaveErrors(user)

        sendAccessEmail(user)

        user
    }

    private void sendAccessEmail(User user) {
        String body = "You can access NF-Tower <a href=\"${buildAccessUrl(user)}\">here</a>"

        Mail mail = Mail.of([to: user.email, subject: 'NF-Tower Access Link', body: body])

        mailService.sendMail(mail)
    }

    private String buildAccessUrl(User user) {
        String accessUrl = "${frontendUrl}/login?email=${user.email}&authToken=${user.authToken}"

        new URI(accessUrl).toString()
    }

    @CompileDynamic
    User findByEmailAndAuthToken(String username, String authToken) {
        User.findByEmailAndAuthToken(username, authToken)
    }

    @CompileDynamic
    List<String> findAuthoritiesByEmail(String email) {
        User user = User.findByEmail(email)
        List<UserRole> rolesOfUser = UserRole.findAllByUser(user)

        rolesOfUser.role.authority
    }

    @CompileDynamic
    private User createUser(String email, String authority) {
        String username = email.replaceAll(/@.*/, '')
        String authToken = UUID.randomUUID().toString()
        Role role = Role.findByAuthority(authority) ?: createRole(authority)

        User user = new User(username: username, email: email, authToken: authToken)
        user.save()

        UserRole userRole = new UserRole(user: user, role: role)
        userRole.save()

        user
    }

    private Role createRole(String authority) {
        Role role = new Role(authority: authority)
        role.save()

        role
    }

    private void checkUserSaveErrors(User user) {
        if (!user.hasErrors()) {
            return
        }

        List<FieldError> fieldErrors = user.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            throw new ValidationException("Can't save a user without ${nullableError.field}")
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            throw new ValidationException("Can't save a user with the same ${uniqueError.field} of another")
        }

        FieldError emailError = fieldErrors.find { it.code == 'email.invalid' }
        if (emailError) {
            throw new ValidationException("Can't save a user with bad ${emailError.field} format")
        }

        List<String> uncustomizedErrors = fieldErrors.collect { "${it.field}|${it.code}".toString() }
        throw new ValidationException("Can't save task. Validation errors: ${uncustomizedErrors}")
    }

}

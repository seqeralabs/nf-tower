package io.seqera.watchtower.service.auth

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.auth.Role
import io.seqera.watchtower.domain.auth.User
import io.seqera.watchtower.domain.auth.UserRole
import org.springframework.validation.FieldError

import javax.inject.Singleton
import javax.validation.ValidationException

@Singleton
@Transactional
class UserServiceImpl implements UserService {


    @CompileDynamic
    User register(String email) {
        User existingUser = User.findByEmail(email)

        if (existingUser) {
            return existingUser
        }

        User user = createUser(email, 'ROLE_USER')
        checkUserSaveErrors(user)

        user
    }

    @CompileDynamic
    User findByUsernameAndAuthToken(String username, String authToken) {
        User.findByUsernameAndAuthToken(username, authToken)
    }

    @CompileDynamic
    List<String> findAuthoritiesByUsername(String username) {
        User user = User.findByUsername(username)
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

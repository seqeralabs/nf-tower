package io.seqera.watchtower.service.auth

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.auth.Role
import io.seqera.watchtower.domain.auth.User
import io.seqera.watchtower.domain.auth.UserRole

import javax.inject.Singleton

@Singleton
@Transactional
class UserServiceImpl implements UserService {


    @CompileDynamic
    User register(String email) {
        User existingUser = User.findByEmail(email)

        if (existingUser) {
            return existingUser
        }

        createUser(email, 'ROLE_USER')
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

}

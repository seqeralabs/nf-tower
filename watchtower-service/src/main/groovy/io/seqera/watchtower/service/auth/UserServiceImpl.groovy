package io.seqera.watchtower.service.auth

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.auth.User

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

        createUser(email)
    }

    private User createUser(String email) {
        String username = email.replaceAll(/@.*/, '')
        String authToken = UUID.randomUUID().toString()

        User user = new User(username: username, email: email, authToken: authToken)
        user.save()

        user
    }

}

package io.seqera.watchtower.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import io.micronaut.security.authentication.providers.UserState
import org.grails.datastore.gorm.GormEntity

@Entity
@CompileDynamic
class User implements UserState {

    String email
    String username
    String password

    boolean enabled = true
    boolean accountExpired = false
    boolean accountLocked = false
    boolean passwordExpired = false

    static constraints = {
        email(nullable: false, blank: false)
        username(nullable: false, blank: false, unique: true)
        password(nullable: false, blank: false, password: true)
    }

    static mapping = {
        password column: '`password`'
    }

}

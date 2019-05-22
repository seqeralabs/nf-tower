package io.seqera.watchtower.domain.auth

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@CompileDynamic
class User {

    String username
    String email
    String authToken

    String firstName
    String lastName
    String organisation
    String description
    String avatar

    static constraints = {
        email(email: true, unique: true)
        username(unique: true)
        authToken(unique: true)

        firstName(nullable: true)
        lastName(nullable: true)
        organisation(nullable: true)
        description(nullable: true)
        avatar(nullable: true)
    }

    static mapping = {
        version false
    }

}

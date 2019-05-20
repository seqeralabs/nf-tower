package io.seqera.watchtower.domain.auth

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@CompileDynamic
class User {

    String username
    String email

    String firstName
    String lastName
    String organisation
    String description
    String avatar
    String authToken

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

}

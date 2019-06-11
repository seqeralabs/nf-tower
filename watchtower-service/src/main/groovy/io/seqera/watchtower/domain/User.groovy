package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.time.Instant

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'accessTokens'])
@CompileDynamic
class User {

    String userName
    String email
    String authToken
    Instant authTime

    String firstName
    String lastName
    String organization
    String description
    String avatar

    static hasMany = [accessTokens: AccessToken]

    static constraints = {
        email(email: true, unique: true)
        userName(unique: true)
        authToken(unique: true)

        firstName(nullable: true)
        lastName(nullable: true)
        organization(nullable: true)
        description(nullable: true)
        avatar(nullable: true, url: true)
    }

    static mapping = {
        table 'usersec'
        version false
    }

}

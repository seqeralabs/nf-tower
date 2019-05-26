package io.seqera.watchtower.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@CompileDynamic
class UserRole {

    User user
    Role role

    static mapping = {
        version false
    }

}
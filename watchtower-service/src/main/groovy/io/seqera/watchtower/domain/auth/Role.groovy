package io.seqera.watchtower.domain.auth

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@CompileDynamic
class Role {
    String authority

    static constraints = {
        authority(nullable: false, unique: true)
    }
}
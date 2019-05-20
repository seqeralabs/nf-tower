package io.seqera.watchtower.domain

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.GormEntity

@Entity
@CompileDynamic
class Role {
    String authority

    static constraints = {
        authority(nullable: false, unique: true)
    }
}
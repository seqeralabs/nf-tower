/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.domain

import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

/**
 * Model a Team entity grouping multiple users
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@CompileDynamic
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'role', 'users', 'projects'])
class Team {
    Long id
    String name
    String description
    String email
    Role role

    static hasMany = [users: User, projects: Project]

    OffsetDateTime dateCreated
    OffsetDateTime lastUpdated

    static constraints = {
        email(nullable: true,maxSize: 255)
        description(nullable: true)
    }
    
    static mapping = {
        users cascade: "none"
    }
}

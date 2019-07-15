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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import java.time.Instant

import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'workflows', 'accessTokens'])
@CompileDynamic
class User {

    static final USERNAME_REGEX = /^[a-z\d](?:[a-z\d]|-(?=[a-z\d])){0,38}$/

    String userName
    String email
    String authToken
    Instant authTime

    String firstName
    String lastName
    String organization
    String description
    String avatar

    static hasMany = [workflows: Workflow, accessTokens: AccessToken]

    static constraints = {
        email(email: true, unique: true)
        userName(unique: true, blank:false, matches: USERNAME_REGEX)
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

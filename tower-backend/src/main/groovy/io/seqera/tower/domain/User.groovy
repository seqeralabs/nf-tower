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

import java.time.Instant
import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import groovy.transform.Memoized
import io.seqera.util.H8UserOptionsType
import io.seqera.util.HashEncoder

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflows', 'accessTokens'])
@CompileDynamic
class User {

    static final USERNAME_REGEX = /^[a-z\d](?:[a-z\d]|-(?=[a-z\d])){0,38}$/

    Long id
    String userName
    String email
    String authToken
    Instant authTime

    String firstName
    String lastName
    String organization
    String description
    String avatar

    boolean trusted
    Boolean disabled
    Boolean notification

    UserOptions options

    OffsetDateTime dateCreated
    OffsetDateTime lastUpdated
    OffsetDateTime lastAccess

    @JsonIgnore
    AccessToken getDefaultAccessToken() {
        accessTokens.find { it.name == AccessToken.DEFAULT_TOKEN }
    }

    static hasMany = [workflows: Workflow, accessTokens: AccessToken]

    static constraints = {
        email(email: true, unique: true, maxSize: 255)
        userName(unique: true, blank:false, matches: USERNAME_REGEX, maxSize: 40)
        authToken(unique: true, nullable: true, maxSize: 40)
        authTime(nullable: true)
        disabled(nullable: true)
        notification(nullable: true)
        firstName(nullable: true, maxSize: 100)
        lastName(nullable: true, maxSize: 100)
        organization(nullable: true, maxSize: 100)
        description(nullable: true, maxSize: 1000)
        avatar(nullable: true, url: true)
        lastAccess(nullable: true)
        options nullable: true
    }

    static mapping = {
        cache  true
        options type: H8UserOptionsType
    }

    @JsonIgnore
    @Memoized
    String getUid() {
        HashEncoder.encode(id)
    }

    static Long decodeUid(String authId) {
        return HashEncoder.decodeLong(authId)
    }
}

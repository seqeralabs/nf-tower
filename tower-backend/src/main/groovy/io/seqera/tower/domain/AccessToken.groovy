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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
/**
 * Represent a user API access token
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@CompileDynamic
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'user', 'userId'])
class AccessToken {

    static final String DEFAULT_TOKEN = 'default'

    Long id
    String token
    String name
    Instant dateCreated
    Instant lastUsed

    User user
    static belongsTo = [user: User]

    static constraints = {
        name unique: 'user', maxSize: 50
        token unique: true, maxSize: 40
        lastUsed nullable: true
    }

    static mapping = {
        cache true
        user index: 'nxd_token_user_name'
        name index: 'nxd_token_user_name'
    }

}

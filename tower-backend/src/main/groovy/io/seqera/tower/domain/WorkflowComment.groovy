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

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow', 'user'])
@CompileDynamic
class WorkflowComment {

    Long id
    User user
    String text
    Boolean deleted
    OffsetDateTime dateCreated
    OffsetDateTime lastUpdated

    // -- DTO field
    Author author

    @JsonGetter('author')
    Author serializeAuthor() {
        user != null ? new Author(user) : null
    }

    static belongsTo = [workflow: Workflow]

    static constraints = {
        text(maxSize: 2048)
        deleted(nullable: true)
    }

    static mapping = {
        user fetch: 'join'
        autoTimestamp false
    }

    static transients = ['author']

    static class Author {
        Long id
        String displayName
        String organization
        String avatarUrl

        Author() {}

        Author(User user) {
            this.id = user.id
            this.displayName = user.userName
            this.organization = user.organization
            this.avatarUrl = user.avatar
        }
    }
}

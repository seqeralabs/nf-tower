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
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow', 'author'])
@CompileDynamic
class WorkflowComment {

    /*
     * Not sure about this relationship. One side it's required to keep track
     * the author of the comment. On the other hand any user should be able to
     * delete its records without compromising the comments history
     */
    User author
    String text
    OffsetDateTime dateCreated
    OffsetDateTime lastUpdated

    static belongsTo = [workflow: Workflow]

    static constraints = {
        text(maxSize: 2048)
    }

    static mapping = {
        autoTimestamp false
    }
}

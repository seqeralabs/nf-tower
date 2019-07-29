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

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

/**
 * Model workflow progress counters
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow'])
@CompileDynamic
class WorkflowProgress {

    Workflow workflow

    TasksProgress progress

    static embedded = ['progress']

}

/**
 * Model available task statuses progress count
 */
@CompileDynamic
class TasksProgress {

    Long running = 0
    Long submitted = 0
    Long failed = 0
    Long pending = 0
    Long succeeded = 0
    Long cached = 0

    @JsonGetter('total')
    Long getTotal() {
        running + submitted + failed + pending + succeeded + cached
    }

    static transients = ['total']

}

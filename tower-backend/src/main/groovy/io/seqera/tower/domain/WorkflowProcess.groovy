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
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

/**
 * Model a workflow process
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@CompileDynamic
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow'])
class WorkflowProcess {
    Long id
    String name
    Integer position

    static belongsTo = [workflow: Workflow]

    static constraints = {
        name(unique: 'workflow')
    }
}

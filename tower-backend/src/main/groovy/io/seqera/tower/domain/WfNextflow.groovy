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

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.CompileDynamic

/**
 * Model Workflow nextflow attribute holding Nextflow metadata
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileDynamic
class WfNextflow {

    @JsonProperty('version')
    String version_ // <-- GORM mess-up the component `version` field with the entity implicit `version` attribute, add an `_` to avoid name collision
    String build
    Instant timestamp

    static constraints = {
        version_(nullable: true, maxSize: 20)
        build(nullable: true, maxSize: 10)
        timestamp(nullable: true)
    }

    static mapping = {
        version_(column: 'nextflow_version')
    }

}

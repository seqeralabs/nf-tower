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

package io.seqera.watchtower.domain

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

import java.time.Instant

/**
 * Model Workflow nextflow attribute holding Nextflow metadata
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached'])
@CompileDynamic
class NextflowMeta {

    String nextflowVersion
    String build
    Instant timestamp

    @JsonSetter('timestamp')
    void deserializeTimestampInstant(String timestampText) {
        timestamp = timestampText ? Instant.parse(timestampText) : null
    }

    @JsonSetter('version')
    void deserializeNextflowVersion(String version) {
        nextflowVersion = version
    }

    @JsonGetter('version')
    String serializeNextflowVersion() {
        nextflowVersion
    }


    static constraints = {
        nextflowVersion(nullable: true)
        build(nullable: true)
        timestamp(nullable: true)
    }

    static mapping = {
        version false
    }

}

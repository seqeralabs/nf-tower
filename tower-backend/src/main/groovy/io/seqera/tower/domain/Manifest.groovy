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
import com.fasterxml.jackson.annotation.JsonSetter
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic

/**
 * Model workflow manifest attribute
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version'])
@CompileDynamic
class Manifest {

    String nextflowVersion
    String defaultBranch
    String versionNum
    String homePage
    String gitmodules
    String description
    String name
    String mainScript
    String author

    @JsonSetter('version')
    void deserializeVersion(String version) {
        versionNum = version
    }

    @JsonGetter('version')
    String serializeVersion() {
        return versionNum
    }

    static constraints = {
        nextflowVersion(nullable: true)
        defaultBranch(nullable: true)
        versionNum(nullable: true)
        homePage(nullable: true)
        gitmodules(nullable: true)
        description(nullable: true)
        name(nullable: true)
        mainScript(nullable: true)
        author(nullable: true)
    }

}

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


import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.CompileDynamic
/**
 * Model workflow manifest attribute
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileDynamic
class WfManifest {

    String nextflowVersion
    String defaultBranch
    @JsonProperty('version')
    String version_ // <-- GORM mess-up the component `version` field with the entity implicit `version` attribute, add an `_` to avoid name collision
    String homePage
    Boolean gitmodules
    String description
    String name
    String mainScript
    String author


    static constraints = {
        nextflowVersion(nullable: true, maxSize: 20)
        defaultBranch(nullable: true, maxSize: 20)
        version_(nullable: true, maxSize: 20)
        homePage(nullable: true, maxSize: 200)
        gitmodules(nullable: true)
        description(nullable: true, maxSize: 1024)
        name(nullable: true, maxSize: 150)
        mainScript(nullable: true, maxSize: 100)
        author(nullable: true, maxSize: 150)
    }

    static mapping = {
        version_(column: 'manifest_version')
    }

}

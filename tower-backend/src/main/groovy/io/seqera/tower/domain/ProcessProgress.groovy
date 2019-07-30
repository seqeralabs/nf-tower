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

@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow'])
@CompileDynamic
class ProcessProgress implements ProgressState {

    static belongsTo = [workflow: Workflow]

    String process

    Long running = 0l
    Long submitted = 0l
    Long failed = 0l
    Long pending = 0l
    Long succeeded = 0l
    Long cached = 0l

    Long totalCpus = 0l
    Long cpuRealtime = 0l
    Long memory = 0l
    Long diskReads = 0l
    Long diskWrites = 0l
    Double memoryEfficiency = 0d
    Double cpuEfficiency = 0d

}

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
class WorkflowProgress implements ProgressState {

    Workflow workflow

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

    void sumProgressState(ProgressState progressState) {
        running = running + progressState.running
        submitted = submitted + progressState.submitted
        failed = failed + progressState.failed
        pending = pending + progressState.pending
        succeeded = succeeded + progressState.succeeded
        cached = cached + progressState.cached

        totalCpus = totalCpus + progressState.totalCpus
        cpuRealtime = cpuRealtime + progressState.cpuRealtime
        memory = memory + progressState.memory
        diskReads = diskReads + progressState.diskReads
        diskWrites = diskWrites + progressState.diskWrites
        memoryEfficiency = memoryEfficiency + progressState.memoryEfficiency
        cpuEfficiency = cpuEfficiency + progressState.cpuEfficiency
    }

}

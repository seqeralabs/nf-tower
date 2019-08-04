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

    long running
    long submitted
    long failed
    long pending
    long succeeded
    long cached

    long totalCpus
    long cpuTime
    double cpuLoad
    long memoryRss
    long memoryReq
    long readBytes
    long writeBytes
    long volCtxSwitch
    long invCtxSwitch

    long loadTasks
    long loadCpus
    long loadMemory

    @JsonGetter('memoryEfficiency')
    double getMemoryEfficiency() {
        memoryReq!=0 ? memoryRss / memoryReq * 100 : 0
    }

    @JsonGetter('cpuEfficiency')
    double getCpuEfficiency() {
        cpuTime!=0 ? cpuLoad / cpuTime * 100 : 0
    }

    void sumProgress(ProgressState progress) {

        pending += progress.pending
        submitted += progress.submitted
        running += progress.running
        succeeded += progress.succeeded
        failed += progress.failed
        cached += progress.cached

        totalCpus += progress.totalCpus
        cpuTime += progress.cpuTime
        cpuLoad += progress.cpuLoad
        memoryRss += progress.memoryRss
        memoryReq += progress.memoryReq
        readBytes += progress.readBytes
        writeBytes += progress.writeBytes
        volCtxSwitch += progress.volCtxSwitch
        invCtxSwitch += progress.invCtxSwitch

    }

}

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

package io.seqera.tower.exchange.progress

import static io.seqera.tower.enums.TaskStatus.*

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.CompileStatic
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.service.LoadStats
import io.seqera.tower.service.ProgressRow
import io.seqera.tower.service.ProgressState
/**
 * Model workflow progress counters
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class WorkflowProgress implements ProgressState {

    @JsonIgnore
    final Map<TaskStatus, Long> taskCount = new HashMap<>(10)

    long totalCpus
    long cpuTime
    float cpuLoad
    long memoryRss
    long memoryReq
    long readBytes
    long writeBytes
    long volCtxSwitch
    long invCtxSwitch

    long loadTasks
    long loadCpus
    long loadMemory

    long peakCpus
    long peakTasks
    long peakMemory

    @JsonGetter('memoryEfficiency')
    float getMemoryEfficiency() {
        if( memoryReq==0 ) return 0
        return memoryRss / memoryReq * 100 as float
    }

    @JsonGetter('cpuEfficiency')
    float getCpuEfficiency() {
        if( cpuTime==0 ) return 0
        return cpuLoad / cpuTime * 100 as float
    }

    WorkflowProgress plus(ProgressRow row) {

        sumTaskCount(row.status, row.count)

        if( row.status == COMPLETED ) {
            totalCpus += row.totalCpus
            cpuTime += row.cpuTime
            cpuLoad += row.cpuLoad
            memoryRss += row.memoryRss
            memoryReq += row.memoryReq
            readBytes += row.readBytes
            writeBytes += row.writeBytes
            volCtxSwitch += row.volCtxSwitch
            invCtxSwitch += row.invCtxSwitch
        }

        return this
    }

    WorkflowProgress withLoad( LoadStats stats ) {
        this.loadTasks = stats.loadTasks
        this.loadCpus = stats.loadCpus
        this.loadMemory = stats.loadMemory
        this.peakCpus = stats.peakCpus
        this.peakTasks = stats.peakTasks
        this.peakMemory = stats.peakMemory
        return this
    }
}

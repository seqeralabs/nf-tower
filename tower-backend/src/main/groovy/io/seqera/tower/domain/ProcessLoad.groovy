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


import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import groovy.transform.ToString
import io.seqera.tower.service.progress.ProgressRow
import io.seqera.tower.service.progress.ProgressRecord

/**
 * Models the workflow execution metrics aggregated at process level
 */
@Entity
@CompileDynamic
@ToString(includeNames = true, includePackage = false, includes='id,process,cpus,cpuTime,cpuLoad,memoryReq,memoryRss,readBytes,writeBytes,volCtxSwitch,invCtxSwitch,peakCpus,peakTasks,peakMemory,loadTasks,loadCpus,loadMemory,dateCreated,lastUpdated')
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow', 'id'])
class ProcessLoad implements ProgressRecord, Serializable {

    Long id
    static belongsTo = [workflow: Workflow]

    String process

    long cpus
    long cpuTime
    long cpuLoad
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

    OffsetDateTime dateCreated
    OffsetDateTime lastUpdated


    @Deprecated
    ProcessLoad plus(ProgressRow row) {
        assert process==row.process

        incStatus(row.status, row.count)

        cpus += row.totalCpus          // total cpus
        cpuTime += row.cpuTime              // cpus * realtime
        cpuLoad += row.cpuLoad as long      // %cpu * realtime / 100
        memoryRss += row.memoryRss          // peak memory rss used
        memoryReq += row.memoryReq          // total memory requested
        readBytes += row.readBytes          // total bytes read
        writeBytes += row.writeBytes        // total bytes written
        volCtxSwitch += row.volCtxSwitch    // total voluntary ctx switches
        invCtxSwitch += row.invCtxSwitch    // total involuntary ctx switches

        return this
    }

    static constraints = {
        process(maxSize: 255, unique: ['workflow'])
    }

}

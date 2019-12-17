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

import static io.seqera.tower.enums.TaskStatus.*

import java.time.OffsetDateTime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import io.seqera.tower.service.progress.ProgressRow
import io.seqera.tower.service.progress.ProgressRecord
import io.seqera.util.H8ListToStringType

/**
 * Model workflow execution progress aggregate metrics
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Entity
@CompileDynamic
@EqualsAndHashCode
@ToString(includeNames = true, includePackage = false, includes='id,cpus,cpuTime,cpuLoad,memoryReq,memoryRss,readBytes,writeBytes,volCtxSwitch,invCtxSwitch,peakCpus,peakTasks,peakMemory,loadTasks,loadCpus,loadMemory,dateCreated,lastUpdated')
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version','workflow','id'])
class WorkflowLoad implements ProgressRecord, Serializable {

    Long id
    static belongsTo = [workflow: Workflow]

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

    List<String> executors

    OffsetDateTime dateCreated
    OffsetDateTime lastUpdated


    @Deprecated
    WorkflowLoad plus(ProgressRow row) {

        incStatus(row.status, row.count)

        if( row.status == COMPLETED ) {
            cpus += row.totalCpus
            cpuTime += row.cpuTime
            cpuLoad += row.cpuLoad as long
            memoryRss += row.memoryRss
            memoryReq += row.memoryReq
            readBytes += row.readBytes
            writeBytes += row.writeBytes
            volCtxSwitch += row.volCtxSwitch
            invCtxSwitch += row.invCtxSwitch
        }

        return this
    }

    static constraints = {
        executors nullable: true
    }

    static mapping = {
        executors type: H8ListToStringType
    }
}

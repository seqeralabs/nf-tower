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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.seqera.tower.enums.TaskStatus

@ToString(includeNames = true, includes = 'pending,running,submitted,succeeded,failed,cached,totalCpus,cpuTime,cpuLoad,memoryRss,memoryReq,readBytes,writeBytes,volCtxSwitch,invCtxSwitch')
@Entity
@JsonIgnoreProperties(['dirtyPropertyNames', 'errors', 'dirty', 'attached', 'version', 'workflow'])
@CompileDynamic
class ProcessProgress implements ProgressState {

    static belongsTo = [workflow: Workflow]

    String process

    long pending
    long running
    long submitted
    long succeeded
    long failed     // note: includes tasks `aborted`
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

    ProcessProgress sumCols(List cols) {
        assert process==cols[0]

        // aggregate status
        final status = cols[1] as TaskStatus
        final long count = ll(cols[2])
        if( status == RUNNING)
            running += count
        else if( status == SUBMITTED )
            submitted += count
        else if( status == NEW )
            pending += count
        else if( status == COMPLETED )
            succeeded += count
        else if( status == FAILED )
            failed += count
        else if( status == ABORTED )
            failed += count
        else if( status == CACHED )
            cached += count
        else
            throw new IllegalArgumentException("Unknown task status: $status")

        totalCpus += ll(cols[3])    // total cpus
        cpuTime += ll(cols[4])      // cpus * realtime
        cpuLoad += dd(cols[5])      // %cpu * realtime / 100
        memoryRss += ll(cols[6])    // peak memory rss used
        memoryReq += ll(cols[7])    // total memory requested
        readBytes += ll(cols[8])    // total bytes read
        writeBytes += ll(cols[9])   // total bytes written
        volCtxSwitch += ll(cols[10]) // total voluntary ctx switches
        invCtxSwitch += ll(cols[11]) // total involuntary ctx switches

        return this
    }

    @CompileStatic
    private long ll(x) {
        x == null ? 0 : x as long
    }

    @CompileStatic
    private double dd(x) {
        x == null ? 0 : x as double
    }
}

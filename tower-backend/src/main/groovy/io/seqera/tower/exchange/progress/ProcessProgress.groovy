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

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.service.ProgressRow
import io.seqera.tower.service.ProgressState

@CompileStatic
@ToString(includeNames = true, includes = 'pending,running,submitted,succeeded,failed,cached,totalCpus,cpuTime,cpuLoad,memoryRss,memoryReq,readBytes,writeBytes,volCtxSwitch,invCtxSwitch')
class ProcessProgress implements ProgressState {

    @JsonIgnore
    Map<TaskStatus,Long> taskCount = new LinkedHashMap<>(10)

    String process
    long totalCpus
    long cpuTime
    float cpuLoad
    long memoryRss
    long memoryReq
    long readBytes
    long writeBytes
    long volCtxSwitch
    long invCtxSwitch

    ProcessProgress plus(ProgressRow row) {
        assert process==row.process

        sumTaskCount(row.status, row.count)

        totalCpus += row.totalCpus          // total cpus
        cpuTime += row.cpuTime              // cpus * realtime
        cpuLoad += row.cpuLoad              // %cpu * realtime / 100
        memoryRss += row.memoryRss          // peak memory rss used
        memoryReq += row.memoryReq          // total memory requested
        readBytes += row.readBytes          // total bytes read
        writeBytes += row.writeBytes        // total bytes written
        volCtxSwitch += row.volCtxSwitch    // total voluntary ctx switches
        invCtxSwitch += row.invCtxSwitch    // total involuntary ctx switches

        return this
    }

}

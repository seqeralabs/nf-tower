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

package io.seqera.tower.service

import java.time.Duration
import java.time.Instant

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Model a workload execution load and peak stats ie. the number
 * of tasks, cpus, mem, etc executed by a workflow at a point in time
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */

@Slf4j
@CompileStatic
class LoadStats implements Serializable {
    private String workflowId
    private Instant timestamp

    private Map<Long,TaskLoad> load

    private long loadCpus
    private long loadTasks
    private long loadMemory

    private long peakCpus
    private long peakTasks
    private long peakMemory

    protected LoadStats() {}

    LoadStats(String workflowId) {
        this.workflowId = workflowId
        this.timestamp = Instant.now()
        this.load = new HashMap<>()
    }

    String getWorkflowId() { workflowId }
    Instant getTimestamp() { timestamp }
    long getLoadCpus() { loadCpus }
    long getLoadTasks() { loadTasks }
    long getLoadMemory() { loadMemory }
    long getPeakCpus() { peakCpus }
    long getPeakTasks() { peakTasks }
    long getPeakMemory() { peakMemory }


    boolean olderThan(Duration duration) {
        Instant.now().minus(duration) > timestamp
    }

    LoadStats update( Map<Long,TaskLoad> tasks, List<Long> terminated ) {
        // copy current load
        final copy = new HashMap<Long,TaskLoad>(load.size()+tasks.size())
        copy.putAll(this.load)
        // add all task incoming from the event
        copy.putAll(tasks)
        // remove the one marked as terminated
        for( Long taskId : terminated ) {
            if( !copy.remove(taskId) ) {
                // this can happen when a task is acquired in a termination status without
                // showing before as running (ie. quick completion or fail/aborted execution)
                log.trace "Unable to drop taskId=$taskId from load stats for workflow Id=$workflowId"
            }
        }

        // now sum it up
        long cpus = 0
        long count = 0
        long memory = 0
        for( TaskLoad task : copy.values() ) {
            count += 1
            cpus += task.cpus
            memory += task.memory
        }

        // update the progress
        final peakTasks = Math.max(peakTasks, count)
        final peakCpus = Math.max(peakCpus, cpus)
        final peakMemory = Math.max(peakMemory, memory)

        new LoadStats(
                timestamp: Instant.now(),
                workflowId: workflowId,
                load: copy,
                loadCpus: cpus,
                loadTasks: count,
                loadMemory: memory,
                peakCpus: peakCpus,
                peakTasks: peakTasks,
                peakMemory: peakMemory )
    }


}

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

package io.seqera.tower.service.progress

import javax.inject.Singleton
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

import io.seqera.tower.enums.TaskStatus

/**
 * Implements a simple in-memory map store for execution progress
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
class LocalStatsStore implements ProgressStore {

    Map<String, Map<Long,TaskStatus>> taskStatus = new ConcurrentHashMap<>()
    Map<String, ProgressState> workflowCounters = new ConcurrentHashMap<>()
    Map<String, Instant> workflowLastModified = new ConcurrentHashMap<>()

    @Override
    TaskStatus getTaskStatus(String workflowId, Long taskId) {
        taskStatus
                .computeIfAbsent(workflowId, { new ConcurrentHashMap<>() })
                .get(taskId)
    }

    void putTaskStatus(String workflowId, Long taskId, TaskStatus status) {
        taskStatus
                .computeIfAbsent(workflowId, { new ConcurrentHashMap<>() })
                .put(taskId, status)
    }

    void removeTaskStatus(String workflowId, Long taskId ) {
        taskStatus.get(workflowId) ?. remove(taskId)
    }

    @Override
    ProgressState getProgress(String workflowId) {
        return workflowCounters.get(workflowId)
    }

    @Override
    void storeProgress(String workflowId, ProgressState counters) {
        workflowCounters.put(workflowId, counters)
        workflowLastModified.put(workflowId, Instant.now())
    }

    @Override
    void storeTaskStatuses(String workflowId, Map<Long, TaskStatus> statuses) {
        for( Map.Entry<Long,TaskStatus> entry : statuses) {
            if( entry.value==null )
                removeTaskStatus(workflowId, entry.key)
            else
                putTaskStatus(workflowId, entry.key, entry.value)
        }
        // update the timestamp
        workflowLastModified.put(workflowId, Instant.now())
    }

    @Override
    void deleteProgress(String workflowId) {
        workflowCounters.remove(workflowId)
        workflowLastModified.remove(workflowId)
        taskStatus.remove(workflowId)
    }

    @Override
    List<String> findExpiredKeys(Duration duration) {
        def result = new ArrayList()
        for( Map.Entry<String,Instant> entry : workflowLastModified ) {
            if( Instant.now().minus(duration) >= entry.value )
                result.add(entry.key)
        }
        return result
    }

    List<String> getAllKeys() {
        new ArrayList<String>(workflowCounters.keySet())
    }
}

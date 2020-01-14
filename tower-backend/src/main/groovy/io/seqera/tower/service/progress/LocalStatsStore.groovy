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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.trace.TraceProgressData
/**
 * Implements a simple in-memory map store for execution progress
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@Singleton
class LocalStatsStore implements ProgressStore {

    private static final Closure CREATE_LOAD_RECORD = { new WorkflowLoad() }

    @Deprecated Map<String, Map<Long,TaskStatus>> taskStatus = new ConcurrentHashMap<>()
    @Deprecated Map<String, ProgressState> workflowCounters = new ConcurrentHashMap<>()
    Map<String, Instant> workflowLastModified = new ConcurrentHashMap<>()
    Map<String, TraceProgressData> traceData = new ConcurrentHashMap<>()
    Map<String, WorkflowLoad> workflowLoadMap = new ConcurrentHashMap<>()

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
    @Deprecated
    void storeProgress(String workflowId, ProgressState counters) {
        workflowCounters.put(workflowId, counters)
        workflowLastModified.put(workflowId, Instant.now())
    }

    @Deprecated
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
    void deleteData(String workflowId) {
        workflowCounters.remove(workflowId)
        workflowLastModified.remove(workflowId)
        taskStatus.remove(workflowId)
        traceData.remove(workflowId)
        workflowLoadMap.remove(workflowId)
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
        new ArrayList<String>(workflowLastModified.keySet())
    }

    @Override
    TraceProgressData getTraceData(String workflowId) {
        traceData.get(workflowId)
    }

    @Override
    void putTraceData(String workflowId, TraceProgressData data) {
        traceData.put(workflowId, data)
        workflowLastModified.put(workflowId, Instant.now())
    }

    WorkflowLoad getWorkflowLoad(String workflowId) {
        synchronized(workflowLoadMap) {
            workflowLoadMap.get(workflowId)
        }
    }

    @Override
    void updateStats(String workflowId, Set<String> executorNames, List<Task> tasks) {
        def current = workflowLoadMap.computeIfAbsent(workflowId, CREATE_LOAD_RECORD)
        synchronized (workflowLoadMap) {
            for( String it : executorNames )
                current.addExecutor(it)

            for( Task it : tasks )
                current.incStats(it)
        }
    }
}

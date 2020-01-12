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

import java.time.Duration

import io.micronaut.context.annotation.DefaultImplementation
import io.seqera.tower.domain.Task
import io.seqera.tower.domain.WorkflowLoad
import io.seqera.tower.enums.TaskStatus
import io.seqera.tower.exchange.trace.TraceProgressData
/**
 * Define the interface to store progress metadata
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@DefaultImplementation(LocalStatsStore)
interface ProgressStore {

    @Deprecated TaskStatus getTaskStatus(String workflowId, Long taskId)

    @Deprecated ProgressState getProgress(String workflowId)

    @Deprecated void storeProgress(String workflowId, ProgressState counters)

    @Deprecated void storeTaskStatuses(String workflowId, Map<Long, TaskStatus> status)

    void deleteData(String workflowId)

    TraceProgressData getTraceData(String workflowId)

    void putTraceData(String workflowId, TraceProgressData data)

    WorkflowLoad getWorkflowLoad(String workflowId)

    void updateStats(String workflowId, Set<String> executorNames, List<Task> tasks)

    List<String> findExpiredKeys(Duration duration)

    List<String> getAllKeys()
}

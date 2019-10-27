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
import io.seqera.tower.enums.TaskStatus
/**
 * Define the interface to store progress metadata
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@DefaultImplementation(LocalStatsStore)
interface ProgressStore {

    TaskStatus getTaskStatus(String workflowId, Long taskId)

    ProgressState getProgress(String workflowId)

    void storeProgress(String workflowId, ProgressState counters)

    void storeTaskStatuses(String workflowId, Map<Long, TaskStatus> status)

    void deleteProgress(String workflowId)

    List<String> findExpiredKeys(Duration duration)

    List<String> getAllKeys()
}

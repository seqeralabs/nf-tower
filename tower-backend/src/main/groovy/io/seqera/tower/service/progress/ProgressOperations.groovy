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

import io.seqera.tower.domain.Task
import io.seqera.tower.exchange.progress.ProgressData
import io.seqera.tower.exchange.trace.TraceProgressData
/**
 * Defines progress service operations
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
interface ProgressOperations {

    @Deprecated void create(String workflowId, List<String> processNames)

    @Deprecated void updateStats(String workflowId, List<Task> tasks)

    void updateProgress(String workflowId, TraceProgressData progress)

    void aggregateMetrics(String workflowId, List<Task> tasks)

    ProgressData getProgressData(String workflowId)

    @Deprecated void complete(String workflowId)

    List<String> findExpired(Duration duration)

    List<ProgressState> getStats()

}

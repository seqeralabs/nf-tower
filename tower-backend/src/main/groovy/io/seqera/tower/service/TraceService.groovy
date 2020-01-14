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

import io.seqera.tower.domain.Task
import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.exchange.trace.TraceBeginRequest
import io.seqera.tower.exchange.trace.TraceCompleteRequest
import io.seqera.tower.exchange.trace.TraceProgressData
import io.seqera.tower.exchange.trace.TraceRecordRequest
import io.seqera.tower.exchange.trace.TraceTaskRequest
import io.seqera.tower.exchange.trace.TraceWorkflowRequest

interface TraceService {

    String createWorkflowKey()

    @Deprecated Workflow processWorkflowTrace(TraceWorkflowRequest request, User user)

    @Deprecated List<Task> processTaskTrace(TraceTaskRequest request)

    @Deprecated void keepAlive(String workflowId)

    Workflow handleFlowBegin(TraceBeginRequest request, User user)
    Workflow handleFlowComplete(TraceCompleteRequest request, User user)
    void handleTaskTrace(TraceRecordRequest request)
    void heartbeat(String workflowId, TraceProgressData progress)
}


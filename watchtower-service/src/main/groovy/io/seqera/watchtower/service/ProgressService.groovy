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

package io.seqera.watchtower.service

import io.seqera.watchtower.domain.ProcessProgress
import io.seqera.watchtower.domain.TasksProgress

interface ProgressService {

    TasksProgress computeTasksProgress(Long workflowId)

    List<ProcessProgress> computeProcessesProgress(Long workflowId)

}
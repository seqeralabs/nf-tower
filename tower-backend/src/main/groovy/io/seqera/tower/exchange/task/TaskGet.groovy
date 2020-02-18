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

package io.seqera.tower.exchange.task


import io.seqera.tower.domain.Task
import io.seqera.tower.exchange.BaseResponse

class TaskGet implements BaseResponse {

    Task task
    String message

    static TaskGet of(Task task) {
        new TaskGet(task: task)
    }

    static error(String message) {
        new TaskGet(message: message)
    }
}

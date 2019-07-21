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

// TODO rename to ListTaskResponse
class TaskList {

    // TODO could not this be refactored to `List<Task>` ?
    List<TaskGet> tasks
    Long total

    static TaskList of(List<TaskGet> tasks, Long total) {
        new TaskList(tasks: tasks, total: total)
    }

}

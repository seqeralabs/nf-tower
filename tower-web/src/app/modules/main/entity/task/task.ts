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
import {TaskData} from "./task-data";
import {TaskStatus} from "./task-status.enum";

export class Task {

  data: TaskData;

  constructor(json: any) {
    json.task.status = TaskStatus[json.task.status];

    this.data = <TaskData> json.task;
  }

  get statusTag(): string {
    return TaskStatus[this.data.status];
  }

  get isCompleted(): boolean {
    return (this.data.status == TaskStatus.COMPLETED);
  }
}

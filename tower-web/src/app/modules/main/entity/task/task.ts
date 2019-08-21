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
import {FormatterUtil} from "../../util/formatter-util";

export class Task {

  data: TaskData;

  constructor(json: any) {
    json.task.status = TaskStatus[json.task.status];

    this.data = <TaskData> json.task;
  }

  get isCompleted(): boolean {
    return (this.data.status == TaskStatus.COMPLETED);
  }

  get statusTag(): string {
    return TaskStatus[this.data.status];
  }

  /* Storage capacity values */
  get humanizedMemory(): string { return FormatterUtil.humanizeStorageCapacity(this.data.memory, 1) }
  get humanizedVmem(): string { return FormatterUtil.humanizeStorageCapacity(this.data.vmem, 1) }
  get humanizedRss(): string { return FormatterUtil.humanizeStorageCapacity(this.data.rss, 1) }
  get humanizedPeakVmem(): string { return FormatterUtil.humanizeStorageCapacity(this.data.peakVmem, 1) }
  get humanizedPeakRss(): string { return FormatterUtil.humanizeStorageCapacity(this.data.peakRss, 1) }
  get humanizedRchar(): string { return FormatterUtil.humanizeStorageCapacity(this.data.rchar, 1) }
  get humanizedWchar(): string { return FormatterUtil.humanizeStorageCapacity(this.data.wchar, 1) }
  get humanizedSyscr(): string { return FormatterUtil.humanizeStorageCapacity(this.data.syscr, 1) }
  get humanizedSyscw(): string { return FormatterUtil.humanizeStorageCapacity(this.data.syscw, 1) }
  get humanizedReadBytes(): string { return FormatterUtil.humanizeStorageCapacity(this.data.readBytes, 1) }
  get humanizedWriteBytes(): string { return FormatterUtil.humanizeStorageCapacity(this.data.writeBytes, 1) }

  /* Duration values */
  get humanizedTime(): string { return FormatterUtil.humanizeDuration(this.data.time) }
  get humanizedDuration(): string { return FormatterUtil.humanizeDuration(this.data.duration) }
  get humanizedRealtime(): string { return FormatterUtil.humanizeDuration(this.data.realtime) }

  /* Date values */
  get humanizedSubmit(): string { return FormatterUtil.formatDate(this.data.submit) }
  get humanizedStart(): string { return FormatterUtil.formatDate(this.data.start) }
  get humanizedComplete(): string { return FormatterUtil.formatDate(this.data.complete) }

  /* Code values */
  get humanizedExit(): string {
    return this.data.exit == 2147483647 ? '' : `${this.data.exit}`;
  }

}

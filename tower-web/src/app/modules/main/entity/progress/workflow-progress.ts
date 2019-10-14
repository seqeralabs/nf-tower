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
import {ProgressState} from "./progress-state";
import {FormatterUtil} from "../../util/formatter-util";

export class WorkflowProgress {

  data: WorkflowProgressState;

  constructor(json: any) {
    this.data = <WorkflowProgressState> json;
  }

  get totalCpuHours(): string {
    return FormatterUtil.convertDurationToHours(this.data.cpuTime);
  }

  /* Storage capacity values */
  get totalMemoryGb(): string {
    return FormatterUtil.humanizeStorageCapacity(this.data.memoryRss, 2, 'GB');
  }
  get totalDiskReadGb(): string {
    return FormatterUtil.humanizeStorageCapacity(this.data.readBytes, 2, 'GB');
  }
  get totalDiskWriteGb(): string {
    return FormatterUtil.humanizeStorageCapacity(this.data.writeBytes, 2, 'GB');
  }

  get loadCpus(): number {
    return this.data.loadCpus;
  }
  get loadTasks(): number {
    return this.data.loadTasks;
  }
  get peakCpus(): number {
    return this.data.peakCpus;
  }
  get peakTasks(): number {
    return this.data.peakTasks;
  }
}

interface WorkflowProgressState extends ProgressState {

  loadCpus: number;
  loadTasks: number;
  loadMemory: number;

  peakCpus: number;
  peakTasks: number;
  peakMemory: number;

}

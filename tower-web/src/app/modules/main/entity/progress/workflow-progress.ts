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

export class WorkflowProgress {

  data: WorkflowProgressState;

  constructor(json: any) {
    this.data = <WorkflowProgressState> json;
  }

  get totalCpuHours(): string {
    return (this.data.cpuTime / (1000 * 60 * 60)).toFixed(2);
  }

  get totalMemoryGb(): string {
    return (this.data.memoryRss / 1024 / 1024 / 1024).toFixed(2);
  }

  get totalDiskReadGb(): string {
    return (this.data.readBytes / 1024 / 1024 / 1024).toFixed(2);
  }

  get totalDiskWriteGb(): string {
    return (this.data.writeBytes / 1024 / 1024 / 1024).toFixed(2);
  }

  get loadCpus(): number {
    return this.data.loadCpus;
  }

  get loadTasks(): number {
    return this.data.loadTasks;
  }

  get loadMemory(): number {
    return this.data.loadMemory;
  }

  get peakLoadCpus(): number {
    return this.data.peakLoadCpus;
  }

  get peakLoadTasks(): number {
    return this.data.peakLoadTasks;
  }

  get peakLoadMemory(): number {
    return this.data.peakLoadMemory;
  }

}

interface WorkflowProgressState extends ProgressState {

  loadCpus: number;
  loadTasks: number;
  loadMemory: number;

  peakLoadCpus: number;
  peakLoadTasks: number;
  peakLoadMemory: number;

}

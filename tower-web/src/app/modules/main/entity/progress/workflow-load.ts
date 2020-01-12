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
import {ProgressRecord} from "./progress-record";
import {FormatterUtil} from "../../util/formatter-util";

export class WorkflowLoad {

  data: WorkflowLoadData;

  constructor(json: any) {
    this.data = json as WorkflowLoadData;
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
  get executorNames(): string[] {
    return this.data.executors;
  }

  get humanizedCost(): string {
    const value = this.data.cost;
    if( value === null || value === undefined  )
      return '-';
    return value.toFixed(2) + ' $';
  }
}


export interface WorkflowLoadData {

  pending: number;
  running: number;
  cached: number;
  submitted: number;
  succeeded: number;
  failed: number;

  cpus: number;
  cpuTime: number;
  cpuLoad: number;
  memoryRss: number;
  memoryReq: number;
  readBytes: number;
  writeBytes: number;
  volCtxSwitch: number;
  invCtxSwitch: number;
  memoryEfficiency: number;
  cpuEfficiency: number;

  loadCpus: number;
  loadTasks: number;
  loadMemory: number;

  peakCpus: number;
  peakTasks: number;
  peakMemory: number;
  executors: string[];
  cost: number;
}

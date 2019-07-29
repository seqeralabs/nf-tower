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
import {HumanizeDuration, HumanizeDurationLanguage, ILanguage} from "humanize-duration-ts";

export class WorkflowProgress {

  data: ProgressState;

  constructor(json: any) {
    this.data = <ProgressState> json;
  }

  get totalCpuHours(): string {
    return (this.data.cpuRealtime / (1000 * 60 * 60)).toFixed(2);
  }

  get totalMemoryGb(): string {
    return (this.data.memory / 1024 / 1024 / 1024).toFixed(2);
  }

  get totalDiskReadGb(): string {
    return (this.data.diskReads / 1024 / 1024 / 1024).toFixed(2);
  }

  get totalDiskWriteGb(): string {
    return (this.data.diskWrites / 1024 / 1024 / 1024).toFixed(2);
  }

}

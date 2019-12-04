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

export class ProcessLoad {

  process: string;
  data: ProgressRecord;

  constructor(json: any) {
    this.process = json.process;
    this.data = json as ProgressRecord;
  }

  get completeTasks(): number {
    return this.data.succeeded + this.data.cached;
  }

  get completeTasksFmt(): string {
    return this.completeTasks.toLocaleString('EN-us');
  }

  get totalTasks(): number {
    return this.data.pending + this.data.running + this.data.cached + this.data.submitted + this.data.succeeded + this.data.failed;
  }

  get totalTasksFmt(): string {
    return this.totalTasks.toLocaleString('EN-us');
  }
}

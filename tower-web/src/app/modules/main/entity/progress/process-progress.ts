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

export class ProcessProgress {

  process: string;
  data: ProgressState;

  constructor(json: any) {
    this.process = json.process;
    this.data = <ProgressState> json;
  }

  get total(): number {
    return this.data.pending + this.data.running + this.data.cached + this.data.submitted + this.data.succeeded + this.data.failed;
  }

}

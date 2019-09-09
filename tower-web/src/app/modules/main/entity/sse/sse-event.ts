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

import {Workflow} from "../workflow/workflow";
import {Progress} from "../progress/progress";
import {SseError} from "./sse-error";

export class SseEvent {

  userId: string | number;
  workflowId: string | number;

  workflow: Workflow;
  progress: Progress;

  error:SseError;

  constructor(json: any) {
    this.userId = json.userId;
    this.workflowId = json.workflowId;

    if (json.workflow) {
      this.workflow = new Workflow(json.workflow);
    }
    if (json.progress) {
      this.progress = new Progress(json.progress);
    }
    if (json.error) {
      this.error = new SseError(json.error);
    }
  }

  get isError(): boolean {
    return (this.error != null);
  }

  get isWorkflow(): boolean {
    return (this.workflow != null);
  }

  get isProgress(): boolean {
    return (this.progress != null);
  }

}

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

import {WorkflowAction} from "./workflow-action.enum";

export class SseEvent {

  userId: string | number;
  workflowId: string | number;

  action: WorkflowAction;

  message: string;

  constructor(json: any) {
    this.userId = json.userId;
    this.workflowId = json.workflowId;
    this.action = WorkflowAction[<string>json.action];
  }

  get isError(): boolean {
    return (this.message != null);
  }

  get isWorkflowUpdate(): boolean {
    return (this.action == WorkflowAction.WORKFLOW_UPDATE);
  }

  get isProgressUpdate(): boolean {
    return (this.action == WorkflowAction.PROGRESS_UPDATE);
  }

}

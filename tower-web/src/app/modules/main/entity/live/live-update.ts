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

export enum LiveAction {
  WORKFLOW_UPDATE, PROGRESS_UPDATE
}

export class LiveUpdate {

  userId: string | number;
  workflowId: string;
  action: LiveAction;
  message: string;

  constructor(json: any) {
    this.userId = json.userId;
    this.workflowId = json.workflowId;
    this.action = LiveAction[<string>json.action];
  }

  get isError(): boolean {
    return (this.message != null);
  }

  get isWorkflowUpdate(): boolean {
    return (this.action == LiveAction.WORKFLOW_UPDATE);
  }

  get isProgressUpdate(): boolean {
    return (this.action == LiveAction.PROGRESS_UPDATE);
  }

}

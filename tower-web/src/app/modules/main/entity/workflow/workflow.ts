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
import {WorkflowData} from "./workflow-data";
import {WorkflowStatus} from "./workflow-status.enum";
import * as dateFormat from "date-fns/format";
import {Progress} from "../progress/progress";
import {DurationUtil} from "../../util/duration-util";

export class Workflow {

  data: WorkflowData;
  progress: Progress;

  constructor(json: any) {
    this.data = <WorkflowData> json.workflow;

    if (json.progress) {
      this.progress = new Progress(json.progress);
    }
  }

  get isRunning(): boolean {
    return (this.computeStatus() === WorkflowStatus.RUNNING);
  }

  get isSuccessful(): boolean {
    return (this.computeStatus() === WorkflowStatus.SUCCEEDED);
  }

  get isFailed(): boolean {
    return (this.computeStatus() === WorkflowStatus.FAILED);
  }

  get isPartialFailed(): boolean {
    return (this.computeStatus() === WorkflowStatus.PARTIAL_FAILED);
  }

  get isCompleted(): boolean {
    return !this.isRunning;
  }

  private computeStatus(): WorkflowStatus {
    return (!this.data.complete)                               ? WorkflowStatus.RUNNING   :
           (this.data.success && this.data.stats.ignoredCount) ? WorkflowStatus.PARTIAL_FAILED :
           (this.data.success)                                 ? WorkflowStatus.SUCCEEDED :
                                                                 WorkflowStatus.FAILED

  }

  get humanizedDuration(): string {
    return DurationUtil.humanizeDuration(this.data.duration)
  }

  get briefCommitId(): string {
    return this.data.commitId.substring(0, 6)
  }

  get params(): string {
    return JSON.stringify(this.data.params);
  }

  getWorkflowStartDateFormatted(format: string): string {
    return dateFormat(this.data.start, format);
  }

}

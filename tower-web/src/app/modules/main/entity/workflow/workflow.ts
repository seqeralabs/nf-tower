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
import {FormatterUtil} from "../../util/formatter-util";

export class Workflow {

  data: WorkflowData;
  progress: Progress;

  constructor(json: any) {
    this.data = <WorkflowData> json.workflow;

    if (json.progress) {
      this.progress = new Progress(json.progress);
    }
  }

  get id(): string {
    return this.data.id;
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
    return FormatterUtil.humanizeDuration(this.data.duration)
  }

  get briefCommitId(): string {
    return this.data.commitId ? this.data.commitId.substring(0, 6) : null
  }

  get humanizedRevision(): string {
    let result = this.data.revision;
    if( result == null )
      return 'n/a';
    if( this.briefCommitId )
      result += ` (${this.briefCommitId})`;
    return result
  }

  get humanizedContainer(): string {
    if( !this.data.container || !this.data.containerEngine )
      return 'n/a';
    return `${this.data.container} (${this.data.containerEngine})`;
  }

  get params(): string {
    return JSON.stringify(this.data.params);
  }

  get startDateFormatted(): string {
    return FormatterUtil.formatDate(this.data.start);
  }

  get exitStatus(): string {
    return this.data.exitStatus != null ? this.data.exitStatus.toString() : '-';
  }

}

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
import {HumanizeDuration, HumanizeDurationLanguage, ILanguage} from "humanize-duration-ts";
import * as dateFormat from "date-fns/format";
import {Progress} from "../progress/progress";

export class Workflow {

  data: WorkflowData;
  progress: Progress;

  constructor(json: any) {
    this.data = <WorkflowData> json.workflow;
    this.progress = <Progress> json.progress;
  }


  get isStarted(): boolean {
    return (this.computeStatus() === WorkflowStatus.STARTED);
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
    return !this.isStarted;
  }

  private computeStatus(): WorkflowStatus {
    return (!this.data.complete)                               ? WorkflowStatus.STARTED   :
           (this.data.success && this.data.stats.ignoredCount) ? WorkflowStatus.PARTIAL_FAILED :
           (this.data.success)                                 ? WorkflowStatus.SUCCEEDED :
                                                                 WorkflowStatus.FAILED

  }

  get humanizedDuration(): string {
    let language: HumanizeDurationLanguage  = new HumanizeDurationLanguage();
    language.addLanguage('short', <ILanguage> {y: () => 'y', mo: () => 'mo', w: () => 'w', d: () => 'd', h: () => 'h', m: () => 'm', s: () => 's'});

    return new HumanizeDuration(language).humanize(this.data.duration, {language: 'short', delimiter: ' '});
  }

  get briefCommitId(): string {
    return this.data.commitId.substring(0, 6)
  }

  getWorkflowStartDateFormatted(format: string): string {
    return dateFormat(this.data.start, format);
  }

}

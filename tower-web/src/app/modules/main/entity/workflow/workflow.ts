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
import {WorkflowData} from './workflow-data';
import {WorkflowStatus} from './workflow-status.enum';
import {ProgressData} from '../progress/progress-data';
import {FormatterUtil} from '../../util/formatter-util';

export class Workflow {

  data: WorkflowData;
  progress: ProgressData;

  constructor(json: any) {
    this.data = json.workflow as WorkflowData;

    if (json.progress) {
      this.progress = new ProgressData(json.progress);
    }
  }

  get id(): string {
    return this.data.id;
  }

  get isSubmitted(): boolean {
    return this.data.status === WorkflowStatus.SUBMITTED;
  }

  get isRunning(): boolean {
    return (this.data.status === WorkflowStatus.RUNNING);
  }

  get isSuccessful(): boolean {
    return (this.data.status === WorkflowStatus.SUCCEEDED && this.data.stats.ignoredCount===0);
  }

  get isFailed(): boolean {
    return (this.data.status === WorkflowStatus.FAILED);
  }

  get isPartialFailed(): boolean {
    return (this.data.status === WorkflowStatus.SUCCEEDED && this.data.stats.ignoredCount>0);
  }

  get isCompleted(): boolean {
    return this.data.status === WorkflowStatus.SUCCEEDED || this.data.status === WorkflowStatus.FAILED;
  }

  get isUnknownStatus(): boolean {
    return this.data.status == null || this.data.status === WorkflowStatus.UNKNOWN;
  }

  get canRelaunch(): boolean {
    return this.launchId!=null && (this.isCompleted || this.isUnknownStatus);
  }

  get notCanDelete(): boolean {
    return this.isSubmitted || this.isRunning;
  }

  get humanizedDuration(): string {
    return this.data.duration ? FormatterUtil.humanizeDuration(this.data.duration) : '-';
  }

  get briefCommitId(): string {
    return this.data.commitId ? this.data.commitId.substring(0, 6) : null;
  }

  get humanizedRevision(): string {
    let result = this.data.commitId;
    if( result == null )
      return '-';
    if( this.data.revision )
      result += ` (${this.data.revision})`;
    return result;
  }

  get sessionIdFormatted(): string {
    return this.data.sessionId ? this.data.sessionId.toString() : '-';
  }

  get userNameFormatted(): string {
    return this.data.userName ? this.data.userName : '-';
  }

  get workDirFormatted(): string {
    return this.data.workDir ? this.data.workDir : '-';
  }

  get humanizedContainer(): string {
    if( !this.data.container || !this.data.containerEngine )
      return '-';
    return `${this.data.container} (${this.data.containerEngine})`;
  }

  get params(): string {
    return JSON.stringify(this.data.params);
  }

  get startDateFormatted(): string {
    return this.data.start ? FormatterUtil.formatDate(this.data.start) : '-';
  }

  get submitDateFormatted(): string {
    return this.data.submit ? FormatterUtil.formatDate(this.data.submit) : '-';
  }

  get exitStatus(): string {
    return this.data.exitStatus != null ? this.data.exitStatus.toString() : '-';
  }

  get displayName(): string {
    return this.data.manifest && this.data.manifest.name != null ? this.data.manifest.name : this.data.projectName;
  }

  get nextflowVerFormatted(): string {
    if( !this.data.nextflow || !this.data.nextflow.version )
      return '-';
    let result = this.data.nextflow.version;
    if( this.data.nextflow.build )
      result = `${result} build ${this.data.nextflow.build}`;
    return result;
  }

  get executorsFormatted(): string {
    if( this.progress.workflowProgress.executorNames )
      return this.progress.workflowProgress.executorNames.join(',');
    else
      return '-';
  }

  get launchId(): string {
    return this.data.launchId;
  }
}

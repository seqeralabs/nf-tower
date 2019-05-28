import {WorkflowData} from "./workflow-data";
import {Progress} from "./progress";
import {Task} from "../task/task";
import {WorkflowStatus} from "./workflow-status.enum";
import {HumanizeDuration, HumanizeDurationLanguage, ILanguage} from "humanize-duration-ts";
import * as dateFormat from "date-fns/format";

export class Workflow {

  data: WorkflowData;
  progress: Progress;
  tasks: Task[];

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

  get isCompleted(): boolean {
    return (this.isSuccessful || this.isFailed);
  }

  private computeStatus(): WorkflowStatus {
    return (!this.data.complete) ? WorkflowStatus.STARTED   :
           (this.data.success)   ? WorkflowStatus.SUCCEEDED :
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

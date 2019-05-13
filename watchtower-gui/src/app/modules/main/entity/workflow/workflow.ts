import {WorkflowData} from "./workflow-data";
import {Progress} from "./progress";
import {WorkflowStatus} from "./workflow-status.enum";
import {HumanizeDuration, HumanizeDurationLanguage, ILanguage} from "humanize-duration-ts";
import * as dateFormat from "date-fns/format";

export class Workflow {

  data: WorkflowData;
  progress: Progress;

  constructor(json: any) {
    json.workflow.status = WorkflowStatus[json.workflow.status];

    this.data = <WorkflowData> json.workflow;
    this.progress = <Progress> json.progress;
  }


  get isStarted(): boolean {
    return (this.data.status === WorkflowStatus.STARTED);
  }

  get isSuccessful(): boolean {
    return (this.data.status === WorkflowStatus.SUCCEEDED);
  }

  get isFailed(): boolean {
    return (this.data.status === WorkflowStatus.FAILED);
  }

  get isCompleted(): boolean {
    return (this.isSuccessful || this.isFailed);
  }

  get humanizedDuration(): string {
    let language: HumanizeDurationLanguage  = new HumanizeDurationLanguage();
    language.addLanguage('short', <ILanguage> {y: () => 'y', mo: () => 'mo', w: () => 'w', d: () => 'd', h: () => 'h', m: () => 'm', s: () => 's'});

    return new HumanizeDuration(language).humanize(this.data.duration, {language: 'short', delimiter: ' '});
  }

  getWorkflowStartDateFormatted(format: string): string {
    return dateFormat(this.data.startTime, format);
  }

}

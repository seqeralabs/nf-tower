import {WorkflowData} from "./workflow-data";
import {Progress} from "./progress";
import * as moment from "moment";
import {WorkflowStatus} from "./workflow-status.enum";

export class Workflow {

  data: WorkflowData;
  progress: Progress;

  constructor(json: any) {
    json.workflow.status = WorkflowStatus[json.workflow.status];
    json.workflow.submitTime = moment(json.workflow.submitTime);
    json.workflow.startTime = moment(json.workflow.startTime);
    json.workflow.completeTime = json.workflow.completeTime ? moment(json.workflow.completeTime) : null;
    json.workflow.duration = moment.duration(json.workflow.duration, 'seconds');

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


}

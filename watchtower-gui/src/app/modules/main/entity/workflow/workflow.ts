import {WorkflowData} from "./workflow-data";
import {Progress} from "./progress";

export class Workflow {

  data: WorkflowData;
  progress: Progress;

  constructor(json: any) {
    this.data = <WorkflowData> json.workflow;
    this.progress = <Progress> json.progress;
  }

}

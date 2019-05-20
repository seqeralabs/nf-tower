import {Progress} from "../workflow/progress";
import {TaskData} from "./task-data";
import {TaskStatus} from "./task-status.enum";

export class Task {

  data: TaskData;
  progress: Progress;

  constructor(json: any) {
    json.task.status = TaskStatus[json.workflow.status];

    this.data = <TaskData> json.task;
    this.progress = <Progress> json.progress;
  }

}

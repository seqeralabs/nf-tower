import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {WorkflowService} from "../../service/workflow.service";
import { TaskData } from '../../entity/task/task-data';

@Component({
  selector: 'wt-task-details',
  templateUrl: './task-details.component.html',
  styleUrls: ['./task-details.component.scss']
})

export class TaskDetailsComponent implements OnChanges, OnInit {

  @Input() workflowId;
  @Input() taskId;
  @Input() exitCode;
  @Input() env;
  @Input() resTime;
  @Input() resRequest;
  @Input() resUsed;

  task: TaskData;

  constructor(private workflowService: WorkflowService) {
  }

  ngOnChanges(): void {
    if (this.taskId) {
      this.workflowService.getTaskById(this.workflowId, this.taskId).subscribe(data => {
        this.task = data;
      });
    }
  }

  ngOnInit() {
  }

  getAction(): string {
    return this.task && this.task.action && this.task.action !== '-' ? '(action: ' + this.task.action + ')' : '';
  }

}

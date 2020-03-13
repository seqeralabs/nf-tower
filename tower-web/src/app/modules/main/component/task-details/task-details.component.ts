import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {WorkflowService} from "../../service/workflow.service";
import {Task} from '../../entity/task/task';

@Component({
  selector: 'wt-task-details',
  templateUrl: './task-details.component.html',
  styleUrls: ['./task-details.component.scss']
})

export class TaskDetailsComponent implements OnChanges, OnInit {

  @Input() workflowId: string;
  @Input() taskId: number;

  task: Task;

  constructor(private workflowService: WorkflowService) {
  }

  ngOnChanges(): void {
    if (this.taskId) {
      this.workflowService.getTaskById(this.workflowId, this.taskId).subscribe(data => {
        this.task = new Task({task: data});
      });
    }
  }

  ngOnInit() {
    this.task = new Task({});
  }

}

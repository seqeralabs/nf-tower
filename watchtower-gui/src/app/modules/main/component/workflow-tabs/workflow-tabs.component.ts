import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {Task} from "../../entity/task/task";
import {TasksTableComponent} from "../tasks-table/tasks-table.component";

declare var $: any;

@Component({
  selector: 'wt-workflow-tabs',
  templateUrl: './workflow-tabs.component.html',
  styleUrls: ['./workflow-tabs.component.scss']
})
export class WorkflowTabsComponent implements OnInit {

  @Input()
  tasks: Task[];

  @ViewChild(TasksTableComponent)
  private tasksTableComponent: TasksTableComponent;

  constructor() { }

  ngOnInit() {
    this.configureTableAdjustOnShow();
  }

  private configureTableAdjustOnShow(): void {
    $('#tasks-tab').on('shown.bs.tab', () => {
      this.tasksTableComponent.adjustTableColumns();
    })
  }

}

import {Component, Input, OnInit} from '@angular/core';
import {Task} from "../../entity/task/task";

@Component({
  selector: 'wt-tasks-table',
  templateUrl: './tasks-table.component.html',
  styleUrls: ['./tasks-table.component.scss']
})
export class TasksTableComponent implements OnInit {

  @Input()
  tasks: Task[];

  constructor() { }

  ngOnInit() {
  }

}

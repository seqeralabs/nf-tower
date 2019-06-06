import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {Task} from "../../entity/task/task";

declare var $: any;

@Component({
  selector: 'wt-tasks-table',
  templateUrl: './tasks-table.component.html',
  styleUrls: ['./tasks-table.component.scss']
})
export class TasksTableComponent implements OnInit, AfterViewInit {

  @Input()
  tasks: Task[];

  constructor() { }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    $('#tasks-table').DataTable({
      scrollX: true
    });
  }

}

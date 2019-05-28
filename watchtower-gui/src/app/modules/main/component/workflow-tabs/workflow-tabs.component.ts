import {Component, Input, OnInit} from '@angular/core';
import {Task} from "../../entity/task/task";

@Component({
  selector: 'wt-workflow-tabs',
  templateUrl: './workflow-tabs.component.html',
  styleUrls: ['./workflow-tabs.component.scss']
})
export class WorkflowTabsComponent implements OnInit {

  @Input()
  tasks: Task[];


  constructor() { }

  ngOnInit() {
  }

}

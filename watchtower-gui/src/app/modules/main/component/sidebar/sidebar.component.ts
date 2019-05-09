import { Component, OnInit } from '@angular/core';
import {Observable} from "rxjs";
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowService} from "../../service/workflow.service";

@Component({
  selector: 'wt-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  workflows$: Observable<Workflow[]>;

  constructor(private workflowService: WorkflowService) { }

  ngOnInit() {
    this.workflows$ = this.workflowService.list();
  }

}

import { Component, OnInit } from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";
import {Observable} from "rxjs";
import {WorkflowService} from "../../service/workflow.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'wt-workflow-detail',
  templateUrl: './workflow-detail.component.html',
  styleUrls: ['./workflow-detail.component.scss']
})
export class WorkflowDetailComponent implements OnInit {

  workflow: Workflow;

  constructor(private workflowService: WorkflowService, private route: ActivatedRoute) { }

  ngOnInit() {
    this.fetchWorkflow();
  }

  fetchWorkflow(): void {
    const workflowId: string = this.route.snapshot.paramMap.get('id');

    this.workflowService.getWorkflow(workflowId).subscribe((workflow: Workflow) => this.workflow = workflow)
  }

}

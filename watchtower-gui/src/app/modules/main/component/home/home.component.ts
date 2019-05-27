import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {WorkflowService} from "../../service/workflow.service";
import {Workflow} from "../../entity/workflow/workflow";

@Component({
  selector: 'wt-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  isUserAuthenticated: boolean;
  workflows: Workflow[];

  constructor(private authService: AuthService,
              private workflowService: WorkflowService) { }

  ngOnInit() {
    this.isUserAuthenticated = this.authService.isUserAuthenticated;
    if (this.isUserAuthenticated) {
      this.initializeWorkflows();
    }
  }

  private initializeWorkflows(): void {
    this.workflowService.workflows$.subscribe(
      (workflows: Workflow[]) => this.workflows = workflows
    )
  }

}

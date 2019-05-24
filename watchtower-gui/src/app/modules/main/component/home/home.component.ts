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

  isUserLoggedIn: boolean;
  workflows: Workflow[];

  constructor(private authService: AuthService,
              private workflowService: WorkflowService) { }

  ngOnInit() {
    this.isUserLoggedIn = this.authService.isUserLoggedIn;
    if (this.isUserLoggedIn) {
      this.initializeWorkflows();
    }
  }

  private initializeWorkflows(): void {
    this.workflowService.workflows$.subscribe(
      (workflows: Workflow[]) => this.workflows = workflows
    )
  }

}

import { Component, OnInit } from '@angular/core';
import {User} from "../../entity/user/user";
import {AuthService} from "../../service/auth.service";
import {Router} from "@angular/router";
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowService} from "../../service/workflow.service";

@Component({
  selector: 'wt-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  isWelcomeMessageLoaded: boolean;

  user: User;
  workflows: Workflow[];

  constructor(private authService: AuthService,
              private workflowService: WorkflowService,
              private router: Router) {
  }

  ngOnInit() {
    this.authService.user$.subscribe(
      (user: User) => {
        this.user = user;
        this.workflowService.workflows$.subscribe( (workflows: Workflow[]) => {
          this.workflows = workflows;
        });
      }
    )
  }

  get thereAreWorkflows(): boolean {
    return (this.user && this.workflows && (this.workflows.length > 0))
  }

}

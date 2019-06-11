import {AfterContentInit, AfterViewInit, Component, OnInit} from '@angular/core';
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
        if (!this.user) {
          if (this.isAtRoot) {
            this.goToLandingPage();
          }
          return;
        }

        this.workflowService.workflows$.subscribe( (workflows: Workflow[]) => {
          this.workflows = workflows;
        });
      }
    )
  }


  private get isAtRoot(): boolean {
    return (this.router.url == '/');
  }

  private goToLandingPage(): void {
    window.location.replace('/landing');
  }

  get shouldLoadSidebar(): boolean {
    return (this.user && this.areWorkflowsInitiatied)
  }

  get shouldLoadWelcomeMessage(): boolean {
    return (this.user && !this.areWorkflowsInitiatied && this.isAtRoot);
  }

  private get areWorkflowsInitiatied() {
    return (this.workflows && (this.workflows.length > 0));
  }

}

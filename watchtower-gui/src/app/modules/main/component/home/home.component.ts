import {AfterContentInit, AfterViewInit, Component, OnInit} from '@angular/core';
import {User} from "../../entity/user/user";
import {AuthService} from "../../service/auth.service";
import {Router} from "@angular/router";
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowService} from "../../service/workflow.service";
import {ServerSentEventsWorkflowService} from "../../service/server-sent-events-workflow.service";
import {Task} from "../../entity/task/task";
import {SseError} from "../../entity/sse/sse-error";
import {Subscription} from "rxjs";
import {NotificationService} from "../../service/notification.service";

@Component({
  selector: 'wt-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  user: User;
  workflows: Workflow[];
  private liveEventsSubscription: Subscription;

  constructor(private authService: AuthService,
              private workflowService: WorkflowService,
              private serverSentEventsWorkflowService: ServerSentEventsWorkflowService,
              private notificationService: NotificationService,
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

        this.subscribeToWorkflowListLiveEvents();
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

  private subscribeToWorkflowListLiveEvents(): void {
    this.liveEventsSubscription = this.serverSentEventsWorkflowService.connectToWorkflowListLive(this.user).subscribe(
      (workflow: Workflow) => {
        console.log('Live workflow list event received', workflow);
        this.workflowService.updateWorkflow(workflow);
      },
      (error: SseError) => {
        console.log('Live workflow list error event received', error);
        this.notificationService.showErrorNotification(error.message, false);
      }
    );
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

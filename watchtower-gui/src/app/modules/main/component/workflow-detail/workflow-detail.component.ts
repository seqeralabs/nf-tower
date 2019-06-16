/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowService} from "../../service/workflow.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ParamMap} from "@angular/router/src/shared";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";
import {ServerSentEventsWorkflowService} from "../../service/server-sent-events-workflow.service";
import {Task} from "../../entity/task/task";
import {Subscription} from "rxjs";
import {SseError} from "../../entity/sse/sse-error";
import {SseErrorType} from "../../entity/sse/sse-error-type";

@Component({
  selector: 'wt-workflow-detail',
  templateUrl: './workflow-detail.component.html',
  styleUrls: ['./workflow-detail.component.scss']
})
export class WorkflowDetailComponent implements OnInit, OnDestroy {

  workflow: Workflow;
  private liveEventsSubscription: Subscription;

  constructor(private workflowService: WorkflowService,
              private serverSentEventsWorkflowService: ServerSentEventsWorkflowService,
              private notificationService: NotificationService,
              private route: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.unsubscribeFromWorkflowLiveEvents();

      console.log('Getting params');
      const workflowId: string = params.get('id');
      this.fetchWorkflow(workflowId);
    });
  }

  ngOnDestroy(): void {
    this.unsubscribeFromWorkflowLiveEvents();
  }

  private fetchWorkflow(workflowId: string | number): void {
    console.log(`Fetching workflow ${workflowId}`);

    this.workflowService.getWorkflow(workflowId, true).subscribe(
      (workflow: Workflow) => this.reactToWorkflowReceived(workflow),
      (error: HttpErrorResponse) => {
        if (error.status === 404) {
          this.notificationService.showErrorNotification("Workflow doesn't exist");
        }
        this.router.navigate(['/']);
      }
    )
  }

  private reactToWorkflowReceived(workflow: Workflow): void {
    this.workflow = workflow;
    this.workflowService.fetchTasks(workflow).subscribe(
      () => {
        if (workflow.isStarted) {
          this.subscribeToWorkflowDetailLiveEvents(workflow);
        }
      }
    );
  }

  private subscribeToWorkflowDetailLiveEvents(workflow: Workflow): void {
    this.liveEventsSubscription = this.serverSentEventsWorkflowService.connectToWorkflowDetailLive(workflow).subscribe(
      (data: Workflow | Task) => {
        console.log('Live workflow details event received', data);
        this.reactToEvent(data);
      },
      (error: SseError) => {
        console.log('Live workflow details event received', error);
        this.reactToErrorEvent(error);
      }
    );
  }

  private unsubscribeFromWorkflowLiveEvents(): void {
    if (this.liveEventsSubscription) {
      this.liveEventsSubscription.unsubscribe();
      this.liveEventsSubscription = null;
    }
  }

  private reactToEvent(data: Workflow | Task): void {
    if (data instanceof Workflow) {
      this.reactToWorkflowEvent(data);
      this.unsubscribeFromWorkflowLiveEvents();
    } else if (data instanceof Task) {
      this.reactToTaskEvent(data);
    }
  }

  private reactToWorkflowEvent(workflow: Workflow): void {
    this.workflowService.updateWorkflow(workflow);
    this.workflow = workflow;
  }

  private reactToTaskEvent(task: Task): void {
    this.workflowService.updateTask(task, this.workflow);
  }

  private reactToErrorEvent(error: SseError): void {
    this.notificationService.showErrorNotification(error.message);
  }

}

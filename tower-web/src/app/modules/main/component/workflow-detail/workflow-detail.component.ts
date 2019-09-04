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
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";
import {ServerSentEventsService} from "../../service/server-sent-events.service";
import {Subscription} from "rxjs";
import {SseError} from "../../entity/sse/sse-error";
import {SseErrorType} from "../../entity/sse/sse-error-type";
import {Progress} from "../../entity/progress/progress";
import {SseHeartbeat} from "../../entity/sse/sse-heartbeat";

@Component({
  selector: 'wt-workflow-detail',
  templateUrl: './workflow-detail.component.html',
  styleUrls: ['./workflow-detail.component.scss']
})
export class WorkflowDetailComponent implements OnInit, OnDestroy {

  workflow: Workflow;
  private liveEventsSubscription: Subscription;

  constructor(private workflowService: WorkflowService,
              private serverSentEventsWorkflowService: ServerSentEventsService,
              private notificationService: NotificationService,
              private route: ActivatedRoute,
              private router: Router) { }

  ngOnInit() {
    this.route.paramMap.subscribe((params: ParamMap) => {
      this.unsubscribeFromWorkflowLiveEvents();
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
          this.router.navigate(['/']);
        }
      }
    )
  }

  private reactToWorkflowReceived(workflow: Workflow): void {
    this.workflow = workflow;
    if (this.workflow.isRunning) {
      this.subscribeToWorkflowLiveEvents(workflow);
    }
  }

  private subscribeToWorkflowLiveEvents(workflow: Workflow): void {
    this.liveEventsSubscription = this.serverSentEventsWorkflowService.connectToWorkflowLiveStream(workflow).subscribe(
      (data: Workflow | Progress) => this.reactToEvent(data),
      (error: SseError) => this.reactToErrorEvent(error)
    );
  }

  private unsubscribeFromWorkflowLiveEvents(): void {
    if (this.liveEventsSubscription) {
      this.liveEventsSubscription.unsubscribe();
      this.liveEventsSubscription = null;
    }
  }

  private reactToEvent(data: Workflow | Progress | SseHeartbeat): void {
    if (data instanceof Workflow) {
      console.log('Live workflow event received', data);
      this.reactToWorkflowEvent(data);
      this.unsubscribeFromWorkflowLiveEvents();
    } else if (data instanceof Progress) {
      console.log('Live workflow event received', data);
      this.reactToProgressEvent(data);
    } else if (data instanceof SseHeartbeat) {
      console.log('Heartbeat event received', data);
    }
  }

  private reactToWorkflowEvent(workflow: Workflow): void {
    this.workflowService.updateWorkflow(workflow);
    this.workflow = workflow;
  }

  private reactToProgressEvent(progress: Progress): void {
    this.workflowService.updateProgress(progress, this.workflow);
  }

  private reactToErrorEvent(error: SseError): void {
    console.log('Live workflow error event received', error);
    if (error.type == SseErrorType.TIMEOUT) {
      this.notificationService.showErrorNotification('Live connection timed out');
    }
  }

}

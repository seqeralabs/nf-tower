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
import {Progress} from "../../entity/progress/progress";
import {SseEvent} from "../../entity/sse/sse-event";

@Component({
  selector: 'wt-workflow-detail',
  templateUrl: './workflow-detail.component.html',
  styleUrls: ['./workflow-detail.component.scss']
})
export class WorkflowDetailComponent implements OnInit, OnDestroy {

  workflow: Workflow;
  private workflowEventsSubscription: Subscription;

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
    this.workflowEventsSubscription = this.serverSentEventsWorkflowService.connectToWorkflowEventsStream(workflow).subscribe(
      (event: SseEvent) => this.reactToEvent(event),
      (event: SseEvent) => this.reactToErrorEvent(event)
    );
  }

  private unsubscribeFromWorkflowLiveEvents(): void {
    if (this.workflowEventsSubscription) {
      this.workflowEventsSubscription.unsubscribe();
      this.workflowEventsSubscription = null;
    }
  }

  private reactToEvent(event: SseEvent): void {
    console.log('Live workflow event received', event);
    if (event.isWorkflow) {
      this.reactToWorkflowEvent(event.workflow);
      this.unsubscribeFromWorkflowLiveEvents();
    } else if (event.isProgress) {
      this.reactToProgressEvent(event.progress);
    }
  }

  private reactToWorkflowEvent(workflow: Workflow): void {
    this.workflowService.updateWorkflow(workflow);
    this.workflow = workflow;
  }

  private reactToProgressEvent(progress: Progress): void {
    this.workflowService.updateProgress(progress, this.workflow);
  }

  private reactToErrorEvent(event: SseEvent): void {
    console.log('Live workflow error event received', event);
    this.notificationService.showErrorNotification(event.error.message);
  }

}

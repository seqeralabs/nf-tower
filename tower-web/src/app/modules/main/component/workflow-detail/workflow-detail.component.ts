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
import {LiveEventsService} from "../../service/live-events.service";
import {Subscription} from "rxjs";
import {ProgressData} from "../../entity/progress/progress-data";
import {LiveUpdate} from "../../entity/live/live-update";

@Component({
  selector: 'wt-workflow-detail',
  templateUrl: './workflow-detail.component.html',
  styleUrls: ['./workflow-detail.component.scss']
})
export class WorkflowDetailComponent implements OnInit, OnDestroy {

  workflow: Workflow;
  private workflowEventsSubscription: Subscription;

  constructor(private workflowService: WorkflowService,
              private serverSentEventsWorkflowService: LiveEventsService,
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

  private fetchWorkflow(workflowId: string): void {
    console.log(`Fetching workflow ${workflowId}`);

    this.workflowService.getWorkflow(workflowId, true).subscribe(
      (workflow: Workflow) => this.reactToWorkflowReceived(workflow),
      (resp: HttpErrorResponse) => {
          this.notificationService.showErrorNotification(resp.error.message);
      }
    );
  }

  private reactToWorkflowReceived(workflow: Workflow): void {
    this.workflow = workflow;
    if (this.workflow.isRunning || this.workflow.isSubmitted) {
      this.subscribeToWorkflowLiveEvents(workflow);
    }
  }

  private subscribeToWorkflowLiveEvents(workflow: Workflow): void {
    if (this.workflowEventsSubscription) {
      this.workflowEventsSubscription.unsubscribe();
    }
    this.workflowEventsSubscription = this.serverSentEventsWorkflowService.connectToWorkflowEventsStream(workflow).subscribe(
      (event: LiveUpdate) => this.reactToEvent(event),
      (event: LiveUpdate) => this.reactToErrorEvent(event)
    );
  }

  private unsubscribeFromWorkflowLiveEvents(): void {
    if (this.workflowEventsSubscription) {
      this.workflowEventsSubscription.unsubscribe();
      this.workflowEventsSubscription = null;
    }
  }

  private reactToEvent(event: LiveUpdate): void {
    if (event.isWorkflowUpdate) {
      // console.log(`** React to workflow update - current workflow id=${this.workflow.id} - received workflow id=${event.workflowId}`)
      this.reactToWorkflowUpdateEvent(event);
    } else if (event.isProgressUpdate) {
      this.reactToProgressUpdateEvent(event);
    }
  }

  private reactToWorkflowUpdateEvent(event: LiveUpdate): void {
    this.workflowService.getWorkflow(event.workflowId, true).subscribe((workflow: Workflow) => {
      this.workflowService.updateWorkflow(workflow);
      this.workflow = workflow;
    });

  }

  private reactToProgressUpdateEvent(event: LiveUpdate): void {
    this.workflowService.getProgress(event.workflowId).subscribe((progress: ProgressData) => {
      this.workflowService.updateProgress(progress, this.workflow);
    });
  }

  private reactToErrorEvent(event: LiveUpdate): void {
    console.log('Live workflow error event received', event);
    this.notificationService.showErrorNotification(event.message);
  }

}

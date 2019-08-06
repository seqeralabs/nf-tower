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

import {Component, OnInit} from '@angular/core';
import {User} from "../../entity/user/user";
import {AuthService} from "../../service/auth.service";
import {Router} from "@angular/router";
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowService} from "../../service/workflow.service";
import {ServerSentEventsWorkflowService} from "../../service/server-sent-events-workflow.service";
import {SseError} from "../../entity/sse/sse-error";
import {Subscription} from "rxjs";
import {NotificationService} from "../../service/notification.service";
import {SseHeartbeat} from "../../entity/sse/sse-heartbeat";
import {FilteringParams} from "../../util/filtering-params";
import {intersectionBy, concat} from "lodash";

@Component({
  selector: 'wt-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  user: User;
  workflows: Workflow[];
  private liveEventsSubscription: Subscription;

  shouldLoadLandingPage: boolean;

  searchingText: string;
  offset: number = 0;
  isSearchTriggered: boolean;
  isLoadingNextPage: boolean;

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
          this.shouldLoadLandingPage = this.isAtRoot;
          return;
        }

        this.workflowService.workflows$.subscribe( (workflows: Workflow[]) => this.reactToWorkflowsEmission(workflows));

      }
    )
  }

  private reactToWorkflowsEmission(emittedWorkflows: Workflow[]): void {
    if (this.isLoadingNextPage && emittedWorkflows.length > 0) {
      console.log('Loading next page', this.isLoadingNextPage, emittedWorkflows);
      this.workflows = concat(this.workflows, emittedWorkflows);
      this.offset += emittedWorkflows.length;

    } else if (!this.isLoadingNextPage && !this.isSearchActive || this.isSearchTriggered) {
      console.log('Searching or updating', this.isLoadingNextPage, this.isSearchActive, this.isSearchTriggered);
      this.workflows = emittedWorkflows;

    } else if (!this.isLoadingNextPage) {
      this.workflows = intersectionBy(this.workflows, emittedWorkflows, (workflow: Workflow) => workflow.data.workflowId);

    }

    this.subscribeToWorkflowListLiveEvents();

    this.isSearchTriggered = false;
    this.isLoadingNextPage = false;
  }

  private subscribeToWorkflowListLiveEvents(): void {
    if (this.liveEventsSubscription) {
      return;
    }

    this.liveEventsSubscription = this.serverSentEventsWorkflowService.connectToWorkflowListLive(this.user).subscribe(
      (data: Workflow | SseHeartbeat) => this.reactToEvent(data),
      (error: SseError) => {
        console.log('Live workflow list error event received', error);
        this.notificationService.showErrorNotification(error.message, false);
      }
    );
  }

  private reactToEvent(data: Workflow | SseHeartbeat) {
    if (data instanceof Workflow) {
      console.log('Live workflow list event received', data);
      this.workflowService.updateWorkflow(data);
    } else if (data instanceof SseHeartbeat) {
      console.log('Heartbeat event received', data);
    }
  }

  private get isAtRoot(): boolean {
    return (this.router.url == '/');
  }

  get shouldLoadSidebar(): boolean {
    return (this.user && (this.isSearchActive || this.areWorkflowsInitiatied) && (this.isAtRoot || this.router.url.startsWith('/workflow')));
  }

  get shouldLoadWelcomeMessage(): boolean {
    return (this.user && (!this.isSearchActive || !this.areWorkflowsInitiatied) && this.isAtRoot);
  }

  private get areWorkflowsInitiatied() {
    return (this.workflows && (this.workflows.length > 0));
  }

  private get isSearchActive(): boolean {
    return (this.searchingText != null  && this.searchingText.length > 0);
  }

  deleteWorkflow(workflowToDelete: Workflow) {
    this.workflowService.deleteWorkflow(workflowToDelete).subscribe(
       () => {},
       (errorMessage: string) => this.notificationService.showErrorNotification(errorMessage)
    );
  }

  searchWorkflows(searchText: string) {
    this.searchingText = searchText;
    this.isSearchTriggered = true;

    this.offset = 0;
    this.workflowService.emitWorkflowsFromServer(new FilteringParams(10, this.offset, searchText));
  }

  onSidebarScroll(event) {
    //Check if the end of the container has been reached: https://stackoverflow.com/a/50038429
    const isScrollEndReached = (event.target.offsetHeight + event.target.scrollTop >= event.target.scrollHeight);
    if (!isScrollEndReached) {
      return;
    }

    console.log('End reached');
    this.loadNewPage();
  }

  private loadNewPage(): void {
    if (this.isLoadingNextPage) {
      return;
    }

    this.isLoadingNextPage = true;
    this.workflowService.emitWorkflowsFromServer(new FilteringParams(10, this.offset + 10, this.searchingText))
  }

}

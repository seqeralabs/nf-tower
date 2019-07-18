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
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Workflow} from "src/app/modules/main/entity/workflow/workflow";
import {environment} from "src/environments/environment";
import {HttpErrorResponse} from "@angular/common/http";
import {HttpClient} from "@angular/common/http";
import {NotificationService} from "src/app/modules/main/service/notification.service";
import {WorkflowService} from "../../service/workflow.service";

@Component({
  selector: 'wt-workflow-card',
  templateUrl: './workflow-card.component.html',
  styleUrls: ['./workflow-card.component.scss']
})
export class WorkflowCardComponent implements OnInit {

  @Input()
  workflow: Workflow;

  @Output()
  workflowDeletion: EventEmitter<Workflow> = new EventEmitter();

  constructor(private httpClient: HttpClient,
              private notificationService: NotificationService,
              private workflowService: WorkflowService) {
  }

  ngOnInit() {

  }

  deleteWorkflow(workflow: Workflow, event) {
    event.stopPropagation();

    let notifier = this.notificationService;
    let emitter = this.workflowDeletion;
    let confirm = prompt(`Please confirm the deletion of the workflow '${workflow.data.runName}' typing its name below (operation is not recoverable):`)
    if( confirm == workflow.data.runName ) {
        this
          .workflowService
          .deleteWorkflow(workflow)
          .subscribe( {
            error(message) { notifier.showErrorNotification(message) },
            complete() { emitter.next(workflow) }
          } )
    }
  }
}

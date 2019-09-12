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
import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {Workflow} from "../modules/main/entity/workflow/workflow";
import {LiveEventsService} from "../modules/main/service/live-events.service";

declare let $: any;

@Component({
  selector: 'wt-workflow-status-icon',
  templateUrl: './workflow-status-icon.component.html',
  styleUrls: ['./workflow-status-icon.component.scss']
})
export class WorkflowStatusIconComponent implements OnInit, AfterViewInit {

  @Input()
  workflow: Workflow;

  isLiveEventsOnline: boolean = true;

  constructor(private liveEventsService: LiveEventsService) {
  }

  ngOnInit() {
    this.subscribeToLiveEventsStatus();
  }

  ngAfterViewInit() {
    this.enableLiveEventsIconTooltip();
  }

  private subscribeToLiveEventsStatus(): void {
    this.liveEventsService.connectionStatus$.subscribe((status: boolean) => {
      if (status == null) {
        return;
      }
      this.isLiveEventsOnline = status;
    })
  }

  private enableLiveEventsIconTooltip(): void {
    $('.live-off').tooltip();
  }

}

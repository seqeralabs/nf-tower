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
import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";
import {interval, Subscription, timer} from "rxjs";
import * as differenceInMilliseconds from "date-fns/difference_in_milliseconds";
import {FormatterUtil} from "../../util/formatter-util";
import {WorkflowService} from "../../service/workflow.service";

@Component({
  selector: 'wt-workflow-stats',
  templateUrl: './workflow-stats.component.html',
  styleUrls: ['./workflow-stats.component.scss']
})
export class WorkflowStatsComponent implements OnInit, OnChanges {

  @Input()
  workflow: Workflow;

  private durationTimerSubscription: Subscription;

  constructor(private workflowService: WorkflowService) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.durationTimerSubscription) {
      this.durationTimerSubscription.unsubscribe();
    }

    if (this.workflow.isCompleted) {
      return;
    }

    this.durationTimerSubscription = interval(1000).subscribe(() => {
      const now: Date = new Date();
      this.workflow.data.duration = differenceInMilliseconds(now, this.workflow.data.start);
    });
  }

}

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
import {DurationUtil} from "../../util/duration-util";

@Component({
  selector: 'wt-workflow-stats',
  templateUrl: './workflow-stats.component.html',
  styleUrls: ['./workflow-stats.component.scss']
})
export class WorkflowStatsComponent implements OnInit, OnChanges {

  @Input()
  workflow: Workflow;
  wallTime: string;

  private wallTimeTimerSubscription: Subscription;

  constructor() {
    this.wallTime = '0';
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes) {
      return;
    }

    if (this.wallTimeTimerSubscription) {
      this.wallTimeTimerSubscription.unsubscribe();
    }

    if (this.workflow.isCompleted) {
      this.wallTime = this.workflow.humanizedDuration;
      return;
    }

    this.wallTimeTimerSubscription = interval(1000).subscribe(() => {
      const now: Date = new Date();
      const difference: number = differenceInMilliseconds(now, this.workflow.data.start);
      this.wallTime = DurationUtil.humanizeDuration(difference);
    });
  }

}

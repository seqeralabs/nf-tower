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
export class WorkflowStatsComponent implements OnChanges {

  @Input()
  workflow: Workflow;

  private durationTimer: Subscription;

  ngOnChanges(changes: SimpleChanges): void {
    if (this.durationTimer) {
      this.durationTimer.unsubscribe();
    }

    let begin: Date;
    if( this.workflow.isSubmitted )
      begin = this.workflow.data.submit;
    else if( this.workflow.isRunning )
      begin = this.workflow.data.start;
    else
      return;

    this.durationTimer = interval(1000).subscribe(() => {
      this.workflow.data.duration = differenceInMilliseconds(new Date(), begin);
    });
  }

}

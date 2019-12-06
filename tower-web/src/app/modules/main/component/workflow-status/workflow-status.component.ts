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
import {getAllTaskStatusesProgressStateTags} from "../../entity/task/task-status.enum";
import {ProgressRecord} from "../../entity/progress/progress-record";
import {FormatterUtil} from "../../util/formatter-util";

@Component({
  selector: 'wt-workflow-status',
  templateUrl: './workflow-status.component.html',
  styleUrls: ['./workflow-status.component.scss']
})
export class WorkflowStatusComponent implements OnInit {

  @Input()
  progressState: ProgressRecord;

  statusesTags: string[];

  constructor() {
    this.statusesTags = getAllTaskStatusesProgressStateTags();
  }

  ngOnInit() {
  }

  numOfTasksFor(status: string): string {
    const result = this.progressState[status];
    return FormatterUtil.humanizeCounter(result);
  }
}

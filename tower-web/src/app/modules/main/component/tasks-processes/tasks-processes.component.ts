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
import {Task} from '../../entity/task/task';
import {groupBy, last, sumBy} from "lodash";
import {HumanizeDuration, HumanizeDurationLanguage, ILanguage} from "humanize-duration-ts";
import {ProcessProgress} from "../../entity/progress/process-progress";
import {getAllTaskStatusesKeys, TaskStatus, toProgressTag} from "../../entity/task/task-status.enum";

@Component({
  selector: 'wt-tasks-processes',
  templateUrl: './tasks-processes.component.html',
  styleUrls: ['./tasks-processes.component.scss']
})
export class TasksProcessesComponent implements OnInit {

  @Input()
  processesProgress: ProcessProgress[];

  statusesTags: string[];

  constructor() {
    this.statusesTags = getAllTaskStatusesKeys().map((statusKey: number) => toProgressTag(statusKey));
  }

  ngOnInit() {
  }

  computePercentageProcessTasks(processProgress: ProcessProgress, progressStatus: string): string {
    const percentage: number = (processProgress.progress[progressStatus] / processProgress.progress.total) * 100;

    return `${percentage}%`;
  }


}

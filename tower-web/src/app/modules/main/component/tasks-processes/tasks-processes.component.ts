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

declare let $: any;

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
    // $('div.tw-task-progress').tooltip();  // this is not working
    $(document).tooltip({ selector: '[data-toggle="tooltip"]'});
  }

  computePercentageProcessTasks(progress: ProcessProgress, status: string): string {
    const percentage: number = (progress.data[status] / progress.total) * 100;

    return `${percentage}%`;
  }

  getTooltipText(progress: ProcessProgress): string {
    let result = '';
    for( const i in this.statusesTags ) {
      const status = this.statusesTags[i];
      const count=progress.data[status];

      if( count>0 ) {
        result += `${this.capitalise(status)}: <b>${count}</b></br>`
      }
    }
    return result;
  }

  private capitalise(s: string): string {
    return (s.length > 0) ? s[0].toUpperCase() + s.substr(1).toLowerCase() : s;
  }
}

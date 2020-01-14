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
import {ProcessLoad} from "../../entity/progress/process-load";
import {getAllTaskStatusesProgressStateTags} from "../../entity/task/task-status.enum";
import {capitalize} from 'lodash';

declare let $: any;

@Component({
  selector: 'wt-tasks-processes',
  templateUrl: './tasks-processes.component.html',
  styleUrls: ['./tasks-processes.component.scss']
})
export class TasksProcessesComponent implements OnInit, OnChanges {

  @Input()
  processesProgress: ProcessLoad[];

  statusesTags: string[];

  private tooltips: any[] = [];

  constructor() {
    this.statusesTags = getAllTaskStatusesProgressStateTags();
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    setTimeout(() => {
      this.tooltips.forEach(tooltipElement => $(tooltipElement).tooltip('dispose'));

      this.tooltips = $('[data-toggle="tooltip"]').get();
      this.tooltips.forEach(tooltipElement => $(tooltipElement).tooltip({trigger: 'manual'}));
    });

  }

  computePercentageProcessTasks(progress: ProcessLoad, status: string): string {
    const percentage: number = (progress.data[status] / progress.totalTasks) * 100;

    return `${percentage}%`;
  }

  getTooltipText(progress: ProcessLoad): string {
    let result = '';
    for( const i in this.statusesTags ) {
      const status = this.statusesTags[i];
      const count=progress.data[status];

      if( count>0 ) {
        result += `${capitalize(status)}: <b>${count}</b></br>`;
      }
    }
    return result;
  }

  onHoverProgressBar(event: MouseEvent) {
    this.toggleProgressBarTooltip(event.target, true);
  }

  onMouseOutProgressBar(event: MouseEvent) {
    this.toggleProgressBarTooltip(event.target, false);
  }

  private toggleProgressBarTooltip(hoveredElement, showOrHide: boolean): void {
    const parent = $(hoveredElement).parents('.tw-task-progress');
    const progressBar = parent.find('[data-toggle="tooltip"]');
    progressBar.tooltip(showOrHide ? 'show' : 'hide');
  }

}

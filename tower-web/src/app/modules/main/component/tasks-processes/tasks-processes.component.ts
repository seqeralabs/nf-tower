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

@Component({
  selector: 'wt-tasks-processes',
  templateUrl: './tasks-processes.component.html',
  styleUrls: ['./tasks-processes.component.scss']
})
export class TasksProcessesComponent implements OnInit, OnChanges {

  @Input()
  processesProgress: ProcessProgress[];

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
  }


  computePercentageProcessCompletedTasks(processProgress: ProcessProgress): string {
    const percentage: number = (processProgress.completedTasks / processProgress.totalTasks) * 100;

    return `${percentage}%`;
  }

  getHumanizedTotalDuration(processProgress: ProcessProgress): string {
    let language: HumanizeDurationLanguage  = new HumanizeDurationLanguage();
    language.addLanguage('short', <ILanguage> {y: () => 'y', mo: () => 'mo', w: () => 'w', d: () => 'd', h: () => 'h', m: () => 'm', s: () => 's'});

    return new HumanizeDuration(language).humanize(processProgress.totalDuration, {language: 'short', delimiter: ' '});
  }

}

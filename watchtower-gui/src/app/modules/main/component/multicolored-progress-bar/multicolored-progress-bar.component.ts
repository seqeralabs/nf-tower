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

import {Component, Input, OnInit} from '@angular/core';
import {TasksProgress} from "../../entity/progress/tasks-progress";

@Component({
  selector: 'wt-multicolored-progress-bar',
  templateUrl: './multicolored-progress-bar.component.html',
  styleUrls: ['./multicolored-progress-bar.component.scss']
})
export class MulticoloredProgressBarComponent implements OnInit {

  @Input()
  tasksProgress: TasksProgress;


  private total: number;

  constructor() { }

  ngOnInit() {
    this.computeTotal();
  }

  private computeTotal(): void {
    this.total = this.tasksProgress.running + this.tasksProgress.submitted + this.tasksProgress.cached + this.tasksProgress.failed + this.tasksProgress.pending + this.tasksProgress.succeeded;
  }

  private getPercentage(value: number): number {
    return value / this.total * 100;
  }


}

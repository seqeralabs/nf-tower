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
import {Progress} from "../../entity/workflow/progress";

@Component({
  selector: 'wt-multicolored-progress-bar',
  templateUrl: './multicolored-progress-bar.component.html',
  styleUrls: ['./multicolored-progress-bar.component.scss']
})
export class MulticoloredProgressBarComponent implements OnInit {

  @Input()
  progress: Progress;


  private total: number;

  constructor() { }

  ngOnInit() {
    this.computeTotal();
  }

  private computeTotal(): void {
    this.total = this.progress.running + this.progress.submitted + this.progress.cached + this.progress.failed + this.progress.pending + this.progress.succeeded;
  }

  private getPercentage(value: number): number {
    return value / this.total * 100;
  }


}

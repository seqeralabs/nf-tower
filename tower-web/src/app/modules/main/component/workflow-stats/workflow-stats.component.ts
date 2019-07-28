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
import {Workflow} from "../../entity/workflow/workflow";

@Component({
  selector: 'wt-workflow-stats',
  templateUrl: './workflow-stats.component.html',
  styleUrls: ['./workflow-stats.component.scss']
})
export class WorkflowStatsComponent implements OnInit {

  @Input()
  workflow: Workflow;

  constructor() { }

  ngOnInit() {
  }

}

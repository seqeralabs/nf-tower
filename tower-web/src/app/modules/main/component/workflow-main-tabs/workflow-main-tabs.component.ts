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
  selector: 'wt-workflow-main-tabs',
  templateUrl: './workflow-main-tabs.component.html',
  styleUrls: ['./workflow-main-tabs.component.scss']
})
export class WorkflowMainTabsComponent implements OnInit {

  @Input()
  workflow: Workflow;

  commandLine: string;
  resolved_config: string;

  constructor() { }

  ngOnInit() {
    this.commandLine = this.workflow.data.commandLine;
    this.resolved_config = this.workflow.data.configText ? this.workflow.data.configText : 'n/a';
  }

}

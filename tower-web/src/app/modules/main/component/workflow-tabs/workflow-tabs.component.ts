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
import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {Task} from "../../entity/task/task";
import {TasksTableComponent} from "../tasks-table/tasks-table.component";
import {Workflow} from "../../entity/workflow/workflow";

declare var $: any;

@Component({
  selector: 'wt-workflow-tabs',
  templateUrl: './workflow-tabs.component.html',
  styleUrls: ['./workflow-tabs.component.scss']
})
export class WorkflowTabsComponent implements OnInit {

  @Input()
  workflow: Workflow;

  @ViewChild(TasksTableComponent)
  private tasksTableComponent: TasksTableComponent;

  constructor() { }

  ngOnInit() {
    this.configureTableAdjustOnShow();
  }

  private configureTableAdjustOnShow(): void {
    $('#tasks-tab').on('shown.bs.tab', () => {
      this.tasksTableComponent.adjustTableColumns();
    })
  }

}

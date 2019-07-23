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
import {chunk} from "lodash";
import {getAllTaskStatusesKeys, TaskStatus, toProgressTag} from "../../entity/task/task-status.enum";

@Component({
  selector: 'wt-workflow-status',
  templateUrl: './workflow-status.component.html',
  styleUrls: ['./workflow-status.component.scss']
})
export class WorkflowStatusComponent implements OnInit {

  @Input()
  tasksProgress: TasksProgress;

  private statusesTags: string[];

  statusesRows: string[][];

  constructor() {
    console.log("All task statuses", getAllTaskStatusesKeys());
    this.statusesTags = getAllTaskStatusesKeys().map((statusKey: number) => toProgressTag(statusKey));
    this.statusesRows = chunk(this.statusesTags, 3)
  }

  ngOnInit() {
  }

}

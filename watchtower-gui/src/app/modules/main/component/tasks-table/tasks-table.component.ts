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
import {AfterContentChecked, AfterViewChecked, AfterViewInit, Component, Input, OnChanges, OnInit} from '@angular/core';
import {Task} from "../../entity/task/task";
import {environment} from "../../../../../environments/environment";
import {Workflow} from "../../entity/workflow/workflow";
import {AuthService} from "../../service/auth.service";

declare var $: any;

const endpointUrl: string = `${environment.apiUrl}`;

@Component({
  selector: 'wt-tasks-table',
  templateUrl: './tasks-table.component.html',
  styleUrls: ['./tasks-table.component.scss']
})
export class TasksTableComponent implements OnInit, OnChanges {

  @Input()
  workflow: Workflow;
  dataTable: any;

  constructor(private authService: AuthService) { }

  ngOnInit() {
  }

  ngOnChanges(): void {
    this.destroyDataTable();
    setTimeout(() => this.initializeDataTable());
  }

  private destroyDataTable(): void {
    if (this.dataTable) {
      this.dataTable.destroy();
    }
  }

  private initializeDataTable(): void {
    this.dataTable = $('#tasks-table').DataTable({
      scrollX: true,
      serverSide: true,
      ajax: {
        url: `${endpointUrl}/workflow/${this.workflow.data.workflowId}/tasks`,
        headers: {
          'Authorization':'Bearer ' + `${this.authService.currentUser.data.jwtAccessToken}`
        },
        dataType: 'json'
      }
    });
  }

  adjustTableColumns(): void {
    if (this.dataTable) {
      this.dataTable.columns.adjust().draw();
    }
  }

}

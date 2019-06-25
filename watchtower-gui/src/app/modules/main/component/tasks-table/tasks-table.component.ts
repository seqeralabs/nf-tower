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
import {Component, Input, OnChanges, OnInit} from '@angular/core';
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
    let jwtAccessToken = this.authService.currentUser.data.jwtAccessToken;

    this.dataTable = $('#tasks-table').DataTable({
      scrollX: true,
      serverSide: true,
      ajax: {
        url: `${endpointUrl}/workflow/${this.workflow.data.workflowId}/tasks`,
        xhrFields: {
          withCredentials: true
        },
        crossDomain: true,
        method: 'POST',
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + `${jwtAccessToken}`,
          "Access-Control-Allow-Origin": "*"
        },
        dataType: 'json',
        data: function (d) {
          return JSON.stringify(d);
        },
        dataFilter: function (data) {
          let json = $.parseJSON(data);
          json.recordsTotal = json.total;
          json.recordsFiltered = json.total;
          json.data = json.tasks
            .map((item) => new Task(item))
            .map((task: Task) => [task.data.taskId, task.data.process, task.data.tag, task.data.status,
              task.data.hash, task.data.cpus, task.data.pcpu, task.data.memory, task.data.pmem, task.data.vmem,
              task.data.rss, task.data.peakVmem, task.data.peakRss, task.data.time, task.data.duration,
              task.data.realtime, task.data.script, task.data.exit, task.data.submit, task.data.start,
              task.data.complete, task.data.rchar, task.data.wchar, task.data.syscr, task.data.syscw,
              task.data.readBytes, task.data.writeBytes, task.data.nativeId, task.data.name, task.data.module,
              task.data.container, task.data.disk, task.data.attempt, task.data.scratch, task.data.workdir]);

          return JSON.stringify(json);
        }
      }
    });
  }

  adjustTableColumns(): void {
    if (this.dataTable) {
      this.dataTable.columns.adjust().draw();
    }
  }

}

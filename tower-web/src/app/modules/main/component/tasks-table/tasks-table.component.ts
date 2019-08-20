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
import {Task} from "../../entity/task/task";
import {Progress} from "../../entity/progress/progress";
import {WorkflowService} from "../../service/workflow.service";

declare var $: any;

@Component({
  selector: 'wt-tasks-table',
  templateUrl: './tasks-table.component.html',
  styleUrls: ['./tasks-table.component.scss']
})
export class TasksTableComponent implements OnInit, OnChanges {

  @Input()
  workflowId: number | string;
  @Input()
  progress: Progress;

  dataTable: any;

  constructor(private workflowService: WorkflowService) {}

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.workflowId) {
      setTimeout(() => {
        this.destroyDataTable();
        this.initializeDataTable();
      });
    } else {
      this.reloadTable();
    }
  }

  private reloadTable(): void {
    this.dataTable.ajax.reload(null, false);
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
      pageLength: 50,
      lengthChange: false,
      orderMulti: false,
      columns: [
        {name: "taskId", className: 'details-control', orderable: true},
        {name: "process", orderable: true},
        {name: "tag", orderable: true},
        {name: "hash", render: (data) => `<span class="inline code">${data}</span>`, orderable: true},
        {name: "status", render: (data) => `<span class="badge badge-pill ${data.toLowerCase()}">${data}</span>`, orderable: true},
        {name: "exit", orderable: false},
        {name: "attempt", orderable: false},
        {name: "container" },
        {name: "nativeId", orderable: false},
        {name: "duration", orderable: false},
        {name: "realtime", orderable: false},
        {name: "submit", orderable: true},
        {name: "start", orderable: true},
        {name: "complete", orderable: true},
        {name: "cpus", orderable: false},
        {name: "pcpu", orderable: false},
        {name: "memory", orderable: false},
        {name: "pmem", orderable: false},
        {name: "rss", orderable: false},
        {name: "vmem", orderable: false},
        {name: "peakRss", orderable: false},
        {name: "peakVmem", orderable: false},
        {name: "time", orderable: false},
        {name: "rchar", orderable: false},
        {name: "wchar", orderable: false},
        {name: "syscr", orderable: false},
        {name: "syscw", orderable: false},
        {name: "readBytes", orderable: false},
        {name: "writeBytes", orderable: false},
        {name: "volCtxt", orderable: false},
        {name: "invCtxt", orderable: false},
        {name: "workdir", render: (data) => `<div class="scrollable code">${data}</div>`, orderable: false, visible: false},
        {name: "script", render: (data) => `<div class="scrollable code">${data}</div>`, orderable: false, visible: false}
      ],
      ajax: {
        url: this.workflowService.buildTasksGetUrl(this.workflowId),
        data: (data) => {
          let filterParams: any = {
            start: data.start,
            length: data.length,
            search: data.search.value,
            order: data.order.map((orderInfo: any) => ({column: data.columns[orderInfo.column].name, dir: orderInfo.dir}))
          };

          return filterParams;
        },
        dataFilter: (data) => {
          let json: any = JSON.parse(data);
          json.recordsTotal = json.total;
          json.recordsFiltered = json.total;
          json.data = json.tasks ? json.tasks
            .map((item) => new Task(item))
            .map((task: Task) => [
              task.data.taskId,
              task.data.process,
              task.data.tag,
              task.data.hash,
              task.statusTag,
              task.humanizedExit,
              task.data.attempt,
              task.data.container,
              task.data.nativeId,
              task.humanizedDuration,
              task.humanizedRealtime,
              task.humanizedSubmit,
              task.humanizedStart,
              task.humanizedComplete,
              task.data.cpus,
              task.data.pcpu,
              task.humanizedMemory,
              task.data.pmem,
              task.humanizedRss,
              task.humanizedVmem,
              task.humanizedPeakRss,
              task.humanizedPeakVmem,
              task.humanizedTime,
              task.humanizedRchar,
              task.humanizedWchar,
              task.humanizedSyscr,
              task.humanizedSyscw,
              task.humanizedReadBytes,
              task.humanizedWriteBytes,
              task.data.volCtxt,
              task.data.invCtxt,
              task.data.workdir,
              task.data.script
            ]) : [];

          return JSON.stringify(json);
        }
      }
    });
    this.attachRowShowEvent();
  }

  private attachRowShowEvent(): void {
    const tableBody = $('#tasks-table tbody');

    tableBody.off('click', 'td.details-control');
    tableBody.on('click', 'td.details-control',(event) => {
      const tr = $(event.target).closest('tr');
      const row = this.dataTable.row(tr);

      if (row.child.isShown()) {
        row.child.hide();
        tr.removeClass('shown');
      } else {
        row.child(this.generateRowDataChildFormat(tr)).show();
        tr.addClass('shown');
      }
    });
  }

  private generateRowDataChildFormat(row): string {
    const script: string = this.dataTable.cell(row, 'script:name').data();
    const workdir: string = this.dataTable.cell(row, 'workdir:name').data();

    return `<ul class="details-row">
        <li>
            <span class="details-row-title">Script</span>
            <span class="details-row-data code">${script}</span>
        </li>
        <li>
            <span class="details-row-title">Workdir</span>
            <span class="details-row-data code">${workdir}</span>
        </li>
    </ul>`;
  }

}

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
import {ProgressData} from "../../entity/progress/progress-data";
import {WorkflowService} from "../../service/workflow.service";
import {convertTaskStatusToProgressLabel} from "../../entity/task/task-status.enum";

declare var $: any;

@Component({
  selector: 'wt-tasks-table',
  templateUrl: './tasks-table.component.html',
  styleUrls: ['./tasks-table.component.scss']
})
export class TasksTableComponent implements OnInit, OnChanges {

  @Input()
  workflowId: string;
  @Input()
  progress: ProgressData;

  dataTable: any;
  taskId: number;

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
      pageLength: 30,
      info: false,
      pagingType: "full",
      lengthChange: false,
      orderMulti: false,
      rowId: (rowData) => `tr-${rowData[0]}`,
      columns: [
        {name: "taskId", className: 'details-control', orderable: true, render: (taskId) => `<span class="mdi mdi-plus-circle-outline" data-toggle="modal" data-target="#taskDetailModal"></span>&nbsp&nbsp${taskId}`},
        {name: "process", orderable: false},
        {name: "tag", orderable: false},
        {name: "hash", orderable: false},
        {name: "status", orderable: false, render: (status) => `<span class="badge badge-pill ${status}">${convertTaskStatusToProgressLabel(status)}</span>`},
        {name: "exit", orderable: false},
        {name: "container", orderable: false },
        {name: "nativeId", orderable: false},
        {name: "submit", orderable: false},
        {name: "duration", orderable: false},
        {name: "realtime", orderable: false},
        {name: "pcpu", orderable: false},
        {name: "pmem", orderable: false},
        {name: "peakRss", orderable: false},
        {name: "peakVmem", orderable: false},
        {name: "rchar", orderable: false},
        {name: "wchar", orderable: false},
        {name: "volCtxt", orderable: false},
        {name: "invCtxt", orderable: false}
      ],
      ajax: {
        url: this.workflowService.buildTasksGetUrl(this.workflowId),
        data: (data) => {
          const filterParams: any = {
            start: data.start,
            length: data.length,
            search: data.search.value,
            order: data.order.map((orderInfo: any) => ({column: data.columns[orderInfo.column].name, dir: orderInfo.dir}))
          };

          return filterParams;
        },
        dataFilter: (data) => {
          const json: any = JSON.parse(data);
          json.recordsTotal = json.total;
          json.recordsFiltered = json.total;
          json.data = json.tasks ? json.tasks
            .map((item) => new Task(item))
            .map((task: Task) => [
              task.data.taskId,
              task.data.process,
              task.data.tag,
              task.data.hash,
              task.statusFmt,
              task.exitFmt,
              task.containerFmt,
              task.data.nativeId,
              task.submitFmt,
              task.durationFmt,
              task.realtimeFmt,
              task.pcpuFmt,
              task.data.pmem,
              task.peakRssFmt,
              task.peakVmemFmt,
              task.rcharFmt,
              task.wcharFmt,
              task.volCtxtFmt,
              task.invCtxtFmt
            ]) : [];

          return JSON.stringify(json);
        }
      }
    });
    this.attachRowShowEvent();
  }

  private attachRowShowEvent(): void {
    const tableBody = $('#tasks-table tbody');
    tableBody.off('click', 'td');
    tableBody.on('click', 'td',(event) => {
      event.target.setAttribute('data-toggle', 'modal');
      event.target.setAttribute('data-target', '#taskDetailModal');
      const targetTr = $(event.target).closest('tr');
      const targetRow = this.dataTable.row(targetTr);
      const isRowBeingShown: boolean = targetRow.child.isShown();
      this.dataTable.rows().ids().each(rowId => {
        const row = this.dataTable.row(`#${rowId}`);
        if (row.child.isShown()) {
          row.child.hide();
        }
      });
      if (!isRowBeingShown && this.getIdForTask(targetTr)) {
        targetRow.child(this.getIdForTask(targetTr)).show();
      }
    });
  }

  private getIdForTask(data): string | void {
    this.taskId = this.dataTable.cell(data, 'taskId:name').data();
  }

}

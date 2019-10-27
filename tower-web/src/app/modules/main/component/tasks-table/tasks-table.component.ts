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
      lengthChange: false,
      orderMulti: false,
      rowId: (rowData) => `tr-${rowData[0]}`,
      columns: [
        {name: "taskId", className: 'details-control', orderable: true, render: (taskId) => `<span class="mdi mdi-menu-right"></span>&nbsp&nbsp${taskId}`},
        {name: "process", orderable: false},
        {name: "tag", orderable: false},
        {name: "hash", orderable: false},
        {name: "status", orderable: false, render: (status) => `<span class="badge badge-pill ${status.toLowerCase()}">${convertTaskStatusToProgressLabel(status)}</span>`},
        {name: "exit", orderable: false},
        {name: "container", orderable: false },
        {name: "nativeId", orderable: false},
        {name: "submit", orderable: true},
        {name: "duration", orderable: true},
        {name: "realtime", orderable: true},
        {name: "pcpu", orderable: false},
        {name: "pmem", orderable: false},
        {name: "peakRss", orderable: true},
        {name: "peakVmem", orderable: true},
        {name: "rchar", orderable: true},
        {name: "wchar", orderable: true},
        {name: "volCtxt", orderable: true},
        {name: "invCtxt", orderable: true},
        // hidden columns
        {name: "workdir", visible: false},
        {name: "script", visible: false},
        {name: "name", visible: false},
        {name: "start", visible: false},
        {name: "complete", visible: false},
        {name: "readBytes", visible: false},
        {name: "writeBytes", visible: false},
        {name: "syscr", visible: false},
        {name: "syscw", visible: false},
        {name: "env", visible: false},
        {name: "cpus", visible: false},
        {name: "memory", visible: false},
        {name: "time", visible: false},
        {name: "rss", visible: false},
        {name: "vmem", visible: false},
        {name: "attempt", visible: false},
        {name: "errorAction", visible: false},
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
              task.data.container,
              task.data.nativeId,
              task.humanizedSubmit,
              task.humanizedDuration,
              task.humanizedRealtime,
              task.data.pcpu,
              task.data.pmem,
              task.humanizedPeakRss,
              task.humanizedPeakVmem,
              task.humanizedRchar,
              task.humanizedWchar,
              task.data.volCtxt,
              task.data.invCtxt,
              // hidden columns
              task.data.workdir,
              task.data.script,
              task.data.name,
              task.humanizedStart,
              task.humanizedComplete,
              task.humanizedReadBytes,
              task.humanizedWriteBytes,
              task.humanizedSyscr,
              task.humanizedSyscw,
              task.data.env,
              task.data.cpus,
              task.humanizedMemory,
              task.humanizedTime,
              task.humanizedRss,
              task.humanizedVmem,
              task.data.attempt,
              task.data.errorAction
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
      const targetTr = $(event.target).closest('tr');
      const targetRow = this.dataTable.row(targetTr);

      const isRowBeingShown: boolean = targetRow.child.isShown();

      this.dataTable.rows().ids().each(rowId => {
        const tr = $(`#${rowId}`);
        const row = this.dataTable.row(`#${rowId}`);
        if (row.child.isShown()) {
          row.child.hide();
          tr.find('td.details-control span')
            .removeClass('mdi-menu-down')
            .addClass('mdi-menu-right');
        }
      });

      if (!isRowBeingShown) {
        targetRow.child(this.generateRowDataChildFormat(targetTr)).show();
        targetTr.find('td.details-control span')
                 .removeClass('mdi-menu-right')
                 .addClass('mdi-menu-down');
      }
    });
  }

  private str(x): string {
    return x == null || x == '' ? '-' : x.toString().trim()
  }

  private col(row, col:string) {
    let result = this.dataTable.cell(row, col+':name').data();
    return this.str(result)
  }

  private generateRowDataChildFormat(data): string {
    const taskName: string = this.col(data, 'name');
    const script: string = this.col(data,'script');
    const workdir: string = this.col(data, 'workdir');
    const status = this.col(data, 'status');
    const exitCode = this.col(data, 'exit');
    const attempt = this.col(data, 'attempt');
    const action = this.col(data, 'errorAction');
    const env = this.col(data, 'env');

    let res_requested = [
      {name: 'container', description: 'Container image name used to execute the task'},
      {name: 'queue', description: 'The queue that the executor attempted to run the process on'},
      {name: 'cpus', description: 'The cpus number request for the task execution'},
      {name: 'memory', description: 'The memory request for the task execution'},
      {name: 'disk', description: 'The disk space request for the task execution'},
      {name: 'time', description: 'The time request for the task execution'},
    ];


    let res_time = [
      {name: 'submit', description: 'Timestamp when the task has been submitted'},
      {name: 'start', description: 'Timestamp when the task execution has started'},
      {name: 'complete', description: 'Timestamp when task execution has completed'},
      {name: 'duration', description: 'Time elapsed to complete since the submission i.e. including scheduling time'},
      {name: 'realtime', description: 'Task execution time i.e. delta between completion and start timestamp i.e. compute wall-time'},
    ];
    let res_used = [
      {name: 'pcpu', description: 'Percentage of CPU used by the process' },
      {name: 'rss', description: 'Real memory (resident set) size of the process'},
      {name: 'peakRss', description: 'Peak of real memory'},
      {name: 'vmem', description: 'Virtual memory size of the process'},
      {name: 'peakVmem', description: 'Peak of virtual memory'},
      {name: 'rchar', description: 'Number of bytes the process read, using any read-like system call from files, pipes, tty, etc'},
      {name: 'wchar', description: 'Number of bytes the process wrote, using any write-like system call.'},
      {name: 'readBytes', description: 'Number of bytes the process directly read from disk'},
      {name: 'writeBytes', description: 'Number of bytes the process originally dirtied in the page-cache (assuming they will go to disk later).'},
      {name: 'syscr', description: 'Number of read-like system call invocations that the process performed'},
      {name: 'syscw', description: 'Number of write-like system call invocations that the process performed'},
      {name: 'volCtxt', description: 'Number of voluntary context switches'},
      {name: 'invCtxt', description: 'Number of involuntary context switches'},
    ];


    return `<div class="card">
            <h5 class="card-header">Task: ${taskName}</h5>
            <div class="card-body">
              <h5 class="card-title">Command</h5>
              <p class="card-text"><pre>${script}</pre></p>

              <h5 class="card-title">Status</h5>
              <p class="card-text"><pre>Exit: ${this.str(exitCode)} (${status}) Attempts: ${attempt} ${action!='-' ? '(action: '+action+')' :''}</pre></p>

              <h5 class="card-title">Work directory</h5>
              <p class="card-text"><pre>${workdir}</pre></p>

              <h5 class="card-title">Environment</h5>
              <p class="card-text"><pre>${this.col(data,'env')}</pre></p>

              <h5 class="card-title">Execution time</h5>
              ${this.renderTable(data, res_time)}

              <h5 class="card-title">Resources requested</h5>
              ${this.renderTable(data, res_requested)}
              
              <h5 class="card-title">Resources usage</h5>
              ${this.renderTable(data, res_used)}    
            </div>
          </div>`;
  }

  private renderTable(row, cols: any[]) {
    let result = `<table class="table table-sm table-hover details-table">
                  <tbody>
                  <thead>
                    <tr>
                      <th scope="col" class="c1" >Label</th>
                      <th scope="col" class="c2" >Value</th>
                      <th scope="col">Description</th>
                    </tr>
                  </thead>
                `;

    for( let index in cols ) {
      let entry = cols[index];
      result += `<tr>
                    <th scope="row"><div class="scrollable">${entry.name}</div></th>
                    <td><div class="scrollable">${this.col(row, entry.name)}</div></td>
                    <td><div class="scrollable">${entry.description}</div></td>
                  </tr>`
    }

    return result += '</tbody></table>'
  }

}

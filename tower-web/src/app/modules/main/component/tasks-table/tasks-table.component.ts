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
        {"name":"taskId"},{"name":"process"},{"name":"tag"},{"name":"status"},{"name":"hash"},{"name":"cpus"},{"name":"pcpu"},{"name":"memory"},{"name":"pmem"},{"name":"vmem"},
        {"name":"rss"},{"name":"peakVmem"},{"name":"peakRss"},{"name":"time"},{"name":"duration"},{"name":"realtime"},{"name":"script"},{"name":"exit"},{"name":"submit"},
        {"name":"start"},{"name":"complete"},{"name":"rchar"},{"name":"wchar"},{"name":"syscr"},{"name":"syscw"},{"name":"readBytes"},{"name":"writeBytes"},{"name":"nativeId"},
        {"name":"name"},{"name":"module"},{"name":"container"},{"name":"disk"},{"name":"attempt"},{"name":"scratch"},{"name":"workdir"}],
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
              task.data.taskId, task.data.process, task.data.tag, task.statusTag,
              task.data.hash, task.data.cpus, task.data.pcpu, task.humanizedMemory, task.data.pmem, task.humanizedVmem,
              task.humanizedRss, task.humanizedPeakVmem, task.humanizedPeakRss, task.humanizedTime, task.humanizedDuration,
              task.humanizedRealtime, task.data.script, task.data.exit, task.humanizedSubmit, task.humanizedStart,
              task.humanizedComplete, task.humanizedRchar, task.humanizedWchar, task.humanizedSyscr, task.humanizedSyscw,
              task.humanizedReadBytes, task.humanizedWriteBytes, task.data.nativeId, task.data.name, task.data.module,
              task.data.container, task.data.disk, task.data.attempt, task.data.scratch, task.data.workdir
            ]) : [];

          return JSON.stringify(json);
        }
      }
    });
  }

}

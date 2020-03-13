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
import {TaskData} from "./task-data";
import {convertTaskStatusToProgressTag, TaskStatus} from "./task-status.enum";
import {FormatterUtil} from "../../util/formatter-util";

export class Task {

  data: TaskData;

  constructor(json: any) {
    if( json.task ) json.task.status = TaskStatus[json.task.status];
    this.data = json.task as TaskData;
  }

  get statusTag(): string {
    return this.data && this.data.status ? TaskStatus[this.data.status].toString() : TaskStatus.NEW.toString();
  }

  get nameFmt(): string {
    return this.data ? this.data.name : '-';
  }

  get scriptFmt(): string {
    return this.data ? this.data.script : '-';
  }

  get attemptFmt(): string {
    return this.data && this.data.attempt ? this.data.attempt.toString() : '-';
  }

  get errorActionFmt(): string {
    const x = this.data ? this.data.errorAction : null;
    return x != null && x !== '-' ? '(action: ' + x + ')' : '';
  }

  get workdirFmt(): string {
    return this.data ? this.fmt(this.data.workdir) : '-';
  }

  get envFmt(): string {
    return this.data ? this.fmt(this.data.env) : '-';
  }

  get containerFmt(): string {
    return this.data ? this.fmt(this.data.container) : '-';
  }

  get queueFmt(): string {
    return this.data ? this.fmt(this.data.queue) : '-';
  }

  get cpusFmt(): string {
    return this.data ? this.fmt(this.data.cpus) : '-';
  }

  get executorFmt(): string {
    return this.data ? this.fmt(this.data.executor) : '-';
  }

  get machineTypeFmt(): string {
    return this.data ? this.fmt(this.data.machineType) : '-';
  }

  get cloudZoneFmt(): string {
    return this.data ? this.fmt(this.data.cloudZone) : '-';
  }

  get priceModelFmt(): string {
    return this.data ? this.fmt(this.data.priceModel) : '-';
  }

  get pcpuFmt(): string {
    return this.data ? this.fmt(this.data.pcpu) : '-';
  }

  get volCtxtFmt(): string {
    return this.data ? this.fmt(this.data.volCtxt) : '-';
  }

  get invCtxtFmt(): string {
    return this.data ? this.fmt(this.data.invCtxt) : '-';
  }

  /* Storage capacity values */
  get diskFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.disk, 1) : null;
    return this.fmt(result);
  }

  get memoryFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.memory, 1) : null;
    return this.fmt(result);
  }

  get vmemFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.vmem, 1) : null;
    return this.fmt(result);
  }

  get rssFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.rss, 1) : null;
    return this.fmt(result);
  }

  get peakVmemFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.peakVmem, 1) : null;
    return this.fmt(result);
  }

  get peakRssFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.peakRss, 1) : null;
    return this.fmt(result);
  }

  get rcharFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.rchar, 1) : null;
    return this.fmt(result);
  }

  get wcharFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.wchar, 1) : null;
    return this.fmt(result);
  }
  get syscrFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.syscr, 1) : null;
    return this.fmt(result);
  }

  get syscwFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.syscw, 1) : null;
    return this.fmt(result);
  }

  get readBytesFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.readBytes, 1) : null;
    return this.fmt(result);
  }

  get WriteBytesFmt(): string {
    const result = this.data ? FormatterUtil.humanizeStorageCapacity(this.data.writeBytes, 1) : null;
    return this.fmt(result);
  }

  /* Duration values */
  get timeFmt(): string {
    const result = this.data ? FormatterUtil.humanizeDuration(this.data.time) : null;
    return this.fmt(result);
  }

  get durationFmt(): string {
    const result = this.data ? FormatterUtil.humanizeDuration(this.data.duration) : null;
    return this.fmt(result);
  }

  get realtimeFmt(): string {
    const result = this.data ? FormatterUtil.humanizeDuration(this.data.realtime) : null;
    return this.fmt(result);
  }

  /* Date values */
  get submitFmt(): string {
    const result = this.data ? FormatterUtil.formatDate(this.data.submit) : null;
    return this.fmt(result);
  }

  get startFmt(): string {
    const result = this.data ? FormatterUtil.formatDate(this.data.start) : null;
    return this.fmt(result);
  }

  get completeFmt(): string {
    const result = this.data ? FormatterUtil.formatDate(this.data.complete) : null;
    return this.fmt(result);
  }

  /* Code values */
  get exitFmt(): string {
    if( !this.data )
      return '-';
    return this.data.exit == null || this.data.exit == 2147483647 ? '-' : `${this.data.exit}`;
  }

  get costFmt(): string {
    if( !this.data )
      return '-';
    if( this.data.cost===0 && (this.data.realtime==null || this.data.machineType==null || this.data.cloudZone==null) )
      return '-';
    const result = FormatterUtil.formatMoney(this.data.cost, 10);
    return this.fmt(result);
  }

  private fmt(x): string {
    return x == null || x === '' ? '-' : x.toString().trim();
  }
}

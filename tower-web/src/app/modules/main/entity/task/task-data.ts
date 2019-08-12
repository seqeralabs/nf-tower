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
import {TaskStatus} from "./task-status.enum";

export interface TaskData {

  taskId: number;
  status: TaskStatus;
  hash: string;
  name: string;
  exit: number;
  submit: Date;
  start: Date;
  process: string;
  tag?: any;
  module: any[];
  container: string;
  attempt: number;
  script: string;
  scratch?: any;
  workdir: string;
  queue?: any;
  cpus: number;
  memory?: any;
  disk?: any;
  time?: any;
  env?: any;
  errorAction: string;
  complete: Date;
  duration: number;
  realtime: number;
  pcpu: number;
  rchar: number;
  wchar: number;
  syscr: number;
  syscw: number;
  readBytes: number;
  writeBytes: number;
  pmem: number;
  vmem: number;
  rss: number;
  peakVmem: number;
  peakRss: number;
  volCtxt: number;
  invCtxt: number;
  nativeId: number;
}

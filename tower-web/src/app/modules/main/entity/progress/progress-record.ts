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
export interface ProgressRecord {

  pending: number;
  running: number;
  cached: number;
  submitted: number;
  succeeded: number;
  failed: number;

  cpus: number;
  cpuTime: number;
  cpuLoad: number;
  memoryRss: number;
  memoryReq: number;
  readBytes: number;
  writeBytes: number;
  volCtxSwitch: number;
  invCtxSwitch: number;
  memoryEfficiency: number;
  cpuEfficiency: number;

  loadCpus: number;
  loadTasks: number;
  loadMemory: number;

  peakCpus: number;
  peakTasks: number;
  peakMemory: number;

}

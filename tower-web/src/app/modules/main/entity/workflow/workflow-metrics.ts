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

export interface WorkflowMetrics {

  process: string;

  cpu: ResourceData;
  mem: ResourceData;
  vmem: ResourceData;
  time: ResourceData;
  reads: ResourceData;
  writes: ResourceData;
  cpuUsage: ResourceData
  memUsage: ResourceData;
  timeUsage: ResourceData;

}

export interface ResourceData {

  mean: number;
  min: number;
  q1: number;
  q2: number;
  q3: number;
  max: number;
  minLabel: string;
  maxLabel: string;
  q1Label: string;
  q2Label: string;
  q3Label: string;

}

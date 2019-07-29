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
export interface ProgressState {
  pending: number;
  running: number;
  cached: number;
  submitted: number;
  succeeded: number;
  failed: number;

  totalCpus: number;
  cpuRealtime: number;
  memory: number;
  diskReads: number;
  diskWrites: number;
}

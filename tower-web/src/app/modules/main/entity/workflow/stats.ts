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
export interface Stats {
  computeTimeFmt: string;
  cachedCount: number;
  cachedDuration: number;
  failedDuration: number;
  succeedDuration: number;
  failedCount: number;
  cachedPct: number;
  cachedCountFmt: string;
  succeedCountFmt: string;
  failedPct: number;
  failedCountFmt: string;
  ignoredCountFmt: string;
  ignoredCount: number;
  succeedPct: number;
  succeedCount: number;
  ignoredPct: number;
}

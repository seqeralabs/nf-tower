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
import {HttpParams} from "@angular/common/http";

export class FilteringParams {

  offset: number;
  max: number;
  search: string;

  constructor(max: number, offset: number, search: string) {
    this.max = max;
    this.offset = offset;
    this.search = search;
  }

  toHttpParams(): HttpParams {
    const rawParams: any = {};
    if (this.max != null) rawParams.max = `${this.max}`;
    if (this.offset != null) rawParams.offset = `${this.offset}`;
    if (this.search != null && this.search != '') rawParams.search = this.search;

    return new HttpParams({
      fromObject: rawParams
    });
  }
}

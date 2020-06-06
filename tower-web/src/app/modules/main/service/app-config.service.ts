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

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import {ServiceInfoResponse} from '../entity/service-info';

/*
 * Service that fetches application config information
 * from backend during app initialization
 *
 * See https://juristr.com/blog/2018/01/ng-app-runtime-config/
 */
@Injectable()
export class AppConfigService {
  private appConfig;

  constructor(private http: HttpClient) { }

  loadAppConfig() {
    const url = `${environment.apiUrl}/service-info`;
    return this.http.get<ServiceInfoResponse>(url)
      .toPromise()
      .then(resp => {
        this.appConfig = resp.serviceInfo;
      });
  }

  getConfig() {
    return this.appConfig;
  }
}

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
import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../entity/user/user";
import {AuthService} from "../../service/auth.service";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {environment} from "../../../../../environments/environment";
import {GetDefaultTokenResponse, ListAccessTokensResponse} from "../../entity/access-token";
import {NotificationService} from "../../service/notification.service";

@Component({
  selector: 'wt-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss']
})
export class WelcomeComponent implements OnInit {

  nextflowRunCommand: string;

  nextflowConfig: string;

  accessToken: string;

  constructor(private httpClient: HttpClient, private notificationService: NotificationService) { }

  ngOnInit() {
    this.fetchDefaultToken();
  }

  private makeNextflowCommand(): string {
    const cmd = 'nextflow run hello -with-tower ';
    let endpoint = this.getEndpointUrl();
    return endpoint ? cmd + endpoint : cmd;
  }

  private makeNextflowConfig(token: string): string {
    let endpoint = this.getEndpointUrl();
    let result = 'tower {\n';
    result += `  accessToken = '${token}'\n`;
    if( endpoint != null )
      result += `  endpoint = '${endpoint}'\n`;
    result += '  enabled = true\n';
    result += '}';
    return result;
  }

  private fetchDefaultToken() {
    let url = `${environment.apiUrl}/token/default`;
    this.httpClient.get<GetDefaultTokenResponse>(url)
      .subscribe(
        resp => {
          this.accessToken = resp.token.token;
          this.nextflowConfig = this.makeNextflowConfig(this.accessToken);
          this.nextflowRunCommand = this.makeNextflowCommand();
        },
        (resp: HttpErrorResponse) => {
          this.notificationService.showErrorNotification(resp.error.message);
        }
      );
  }

  private getEndpointUrl(): string {
    const url = new URL(window.location.href);
    const base = url.origin;
    if( !base.endsWith('://tower.nf') ) {
      return base + '/api'
    }
    else {
      return null;
    }
  }
}

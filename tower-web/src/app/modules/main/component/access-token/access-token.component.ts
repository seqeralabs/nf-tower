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
import { Component, OnInit } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { HttpErrorResponse } from "@angular/common/http";
import { Router } from "@angular/router";
import { environment } from "src/environments/environment";
import { NotificationService } from "src/app/modules/main/service/notification.service";
import { DatePipe } from '@angular/common';
import {AccessToken} from 'src/app/modules/main/entity/access-token';
import {ListAccessTokensResponse} from 'src/app/modules/main/entity/access-token';
import {CreateAccessTokenRequest} from 'src/app/modules/main/entity/access-token';
import {CreateAccessTokenResponse} from 'src/app/modules/main/entity/access-token';

@Component({
  selector: 'wt-access-token',
  templateUrl: './access-token.component.html',
  styleUrls: ['./access-token.component.scss'],
  providers: [DatePipe]
})
export class AccessTokenComponent implements OnInit {

  tokens: AccessToken[];
  disabled: boolean;

  constructor(private httpClient: HttpClient,
              private notificationService: NotificationService,
              private router: Router,
              private datePipe: DatePipe) { }

    ngOnInit() {
      let url = `${environment.apiUrl}/token/list`;
      this.httpClient.get<ListAccessTokensResponse>(url)
          .subscribe(
              resp => {
                    this.tokens = resp.tokens;
                    this.disabled = this.tokens.length==0;
              },
              (resp: HttpErrorResponse) => {
                  this.notificationService.showErrorNotification(resp.error.message);
              }
          );
    }

    createNewToken() {
      let name: string = prompt("Token name")
      if( name == null )
          return;
      let req: CreateAccessTokenRequest = {name:name};
      let url = `${environment.apiUrl}/token/create`;
      this.httpClient.post<CreateAccessTokenResponse>(url, req)
          .subscribe(
              resp => {
                    this.tokens.push(resp.token);
                    this.disabled = this.tokens.length==0
              },
              (resp: HttpErrorResponse) => {
                  this.notificationService.showErrorNotification(resp.error.message);
              }
          );
    }

    deleteToken(token: AccessToken) {
      let confirm = prompt(`Please confirm the deletion of the access token '${token.name}' typing its name below (operation is not recoverable):`)
      if( confirm != token.name )
          return;
      let index = this.tokens.indexOf(token)
      let url = `${environment.apiUrl}/token/delete/${token.id}`;
      this.httpClient.delete(url)
          .subscribe(
              resp => {
                    this.tokens.splice(index, 1);
                    this.disabled = this.tokens.length==0
              },
              (resp: HttpErrorResponse) => {
                  this.notificationService.showErrorNotification(resp.error.message);
              }
          );
    }

    deleteAllTokens() {
      let confirm = prompt(`Please confirm the deletion of all access tokens typing 'CONFIRM' below (operation is not recoverable):`)
      if( confirm != 'CONFIRM' )
           return;
      let url = `${environment.apiUrl}/token/delete-all`;
      this.httpClient.delete(url)
          .subscribe(
              resp => {
                  this.tokens = [];
                  this.disabled = true
              },
              (resp: HttpErrorResponse) => {
                  this.notificationService.showErrorNotification(resp.error.message);
              }
          );
    }

    lastUsedFmt(date: Date): string {
      return date ? "Last used " + this.datePipe.transform(date,"medium") : "Never used";
    }
}

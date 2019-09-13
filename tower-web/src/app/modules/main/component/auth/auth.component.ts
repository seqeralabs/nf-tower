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

import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";
import {User} from "../../entity/user/user";
import {delay} from "rxjs/operators";

@Component({
  selector: 'wt-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.scss']
})
export class AuthComponent implements OnInit {

  constructor(private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private notificationService: NotificationService) { }


  ngOnInit() {
    let queryParams: ParamMap = this.route.snapshot.queryParamMap;

    let email: string = queryParams.get('email');
    let authToken: string = queryParams.get('authToken');

    this.doAuth(email, authToken);
  }

  doAuth(email: string, authToken: string): void {
    console.log('Authenticating with', email, authToken);
    this.authService.auth(email, authToken).pipe(
      delay(500)
    ).subscribe(
      (user: User) => this.handleAuthenticationSuccess(user),
      (error: HttpErrorResponse) => this.handleAuthenticationError(error)
    )
  }

  private handleAuthenticationSuccess(user: User): void {
    console.log('User successfully authenticated', user);
    this.router.navigate(['']);
  }

  private handleAuthenticationError(error: HttpErrorResponse): void {
    let errorMessage: string = (error.status == 400 || error.status == 401) ? 'Bad credentials' : `Unexpected error ${error.status}`;
    this.notificationService.showErrorNotification(errorMessage);

    this.router.navigate([''])
  }

}

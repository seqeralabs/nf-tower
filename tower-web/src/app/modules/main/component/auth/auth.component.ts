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

import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
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
export class AuthComponent implements OnInit, AfterViewInit {

  @ViewChild('authForm', {static: true})
  authForm;

  loginPath: string;
  uid: string;
  token: string;
  login = false;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private notificationService: NotificationService) { }

  ngOnInit() {
    const queryParams: ParamMap = this.route.snapshot.queryParamMap;
    const success = queryParams.get('success');
    this.loginPath = this.authService.authEndpointUrl;
    this.uid = queryParams.get('uid');
    this.token = queryParams.get('token');

    // These as the `email` and access token submitted when clicking
    // on the authorization email. These value populate an hidden form
    // and post the them to backend to trigger the login process
    if( this.uid && this.token ) {
      this.login=true;
      // see `ngAfterViewInit` below for the form submission
    }
    // when the parameter `success` is set this component
    // is invoked as result of authorization callback.
    // The `success` flag can be either true or false to signal
    // the corresponding login result
    else if( success ) {
      if( success === 'true' ) {
        this.authService.setAuthorizedUser();
      }
      else {
        console.warn('Login failed');
        this.notificationService.showErrorNotification('Login failed');
        this.router.navigate(['']);
      }
    }
    // Invalid status
    else {
      console.warn('Invalid login request');
      this.notificationService.showErrorNotification('Invalid login request');
      this.router.navigate(['']);
    }
  }

  ngAfterViewInit() {
    if(this.login) {
      setTimeout(() => this.submitAuthForm(), 300);
    }
  }

  private submitAuthForm(): void {
    this.authForm.nativeElement.submit();
  }

}

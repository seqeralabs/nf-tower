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

import {Component, NgZone, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {HttpErrorResponse} from "@angular/common/http";
import {NgForm} from "@angular/forms";
import {AuthService} from "src/app/modules/main/service/auth.service";
import {NotificationService} from "src/app/modules/main/service/notification.service";
import {AccessGateState} from "src/app/modules/main/entity/gate";
import {environment} from "../../../../../environments/environment";

@Component({
  selector: 'wt-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  State = AccessGateState;

  @ViewChild('loginForm', {static: true})
  private loginForm: NgForm;
  private isSubmitted: boolean;

  email: string;
  state: AccessGateState;
  captchaResponse;
  captchaKey = environment.captchaKey;

  constructor(private ngZone: NgZone,
              private authService: AuthService,
              private notificationService: NotificationService) {
    this.isSubmitted = false;
    this.state = null;
  }

  ngOnInit() {
    if( this.captchaKey ) {
      this.addCaptchaScript();
    }
  }

  submit(): void {
    this.isSubmitted = true;
    this
      .authService
      .access(this.email, this.captchaResponse)
      .subscribe(
          (data) => {
            this.state = data.state;
          },
          (resp: HttpErrorResponse) => {
            this.isSubmitted = false;
            this.notificationService.showErrorNotification(resp.error.message);
          }
    );
  }

  isSubmitEnabled(): boolean {
    return !this.isSubmitted && this.loginForm.form.valid && (this.captchaKey==null || this.captchaResponse!=null);
  }

  /*
   * Dynamic load google captcha script
   * https://developers.google.com/recaptcha/docs/display
   */
  private addCaptchaScript(): void {
    // @ts-ignore
    window.onCaptchaOk = (data) => {
      this.ngZone.run(() => {
        this.captchaResponse = data;
      });
    };

    const script = document.createElement('script');
    script.src = `https://www.google.com/recaptcha/api.js`;
    script.async = true;
    script.defer = true;
    document.body.appendChild(script);
  }

}

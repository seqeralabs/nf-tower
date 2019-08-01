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

import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {HttpErrorResponse} from "@angular/common/http";
import {NgForm} from "@angular/forms";
import {AuthService} from "src/app/modules/main/service/auth.service";
import {NotificationService} from "src/app/modules/main/service/notification.service";
import {AccessGateState} from "src/app/modules/main/entity/gate";

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

  constructor(private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private notificationService: NotificationService) {
    this.isSubmitted = false;
    this.state = null;
  }

  ngOnInit() { }

  submit(): void {
    this.isSubmitted = true;

    this
      .authService
      .access(this.email)
      .subscribe(
          (data) => {
            this.state = data.state
            console.log(`>>> OK: ${this.state}`);
          },
          (resp: HttpErrorResponse) => {
            this.isSubmitted = false;
            console.log(`error: ${resp.error}`);
            this.notificationService.showErrorNotification(resp.error.message);
          }
    );
  }

  isSubmitEnabled(): boolean {
    return (!this.isSubmitted && this.loginForm.form.valid);
  }

}

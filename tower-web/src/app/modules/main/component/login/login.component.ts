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
import {AuthService} from "../../service/auth.service";
import {NgForm} from "@angular/forms";
import {NotificationService} from "../../service/notification.service";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'wt-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  @ViewChild('loginForm', {static: true})
  private loginForm: NgForm;
  private isSubmitted: boolean;

  email: string;
  isRegistered: boolean;
  registeredMessage: string;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private notificationService: NotificationService) {
    this.isSubmitted = false;
    this.isRegistered = false;
  }

  ngOnInit() {

  }

  submit(): void {
    this.isSubmitted = true;

    this.authService.register(this.email).subscribe(
      (message) => {
        this.isRegistered = true;
        this.registeredMessage = message;
      },
      (error: HttpErrorResponse) => {
        this.isSubmitted = false;
        this.notificationService.showErrorNotification(error.error);
      }
    );
  }

  isSubmitEnabled(): boolean {
    return (!this.isSubmitted && this.loginForm.form.valid);
  }

}

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
import {ActivatedRoute, ParamMap} from "@angular/router";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'wt-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.scss']
})
export class AuthComponent implements OnInit, AfterViewInit {

  constructor(private route: ActivatedRoute,
              private authService: AuthService) {
  }

  @ViewChild('authForm', {static: true})
  authForm;

  authEndpointUrl: string;
  email: string;
  authToken: string;

  ngOnInit() {
    this.authEndpointUrl = this.authService.authEndpointUrl;
  }

  doAuth(email: string, authToken: string): void {
    const queryParams: ParamMap = this.route.snapshot.queryParamMap;
    this.email = queryParams.get('email');
    this.authToken = queryParams.get('authToken');
    console.log('Authenticating with', email, authToken);
  }

  ngAfterViewInit() {
    setTimeout(() => this.submitAuthForm(), 500);
  }

  private submitAuthForm(): void {
    this.authForm.nativeElement.submit();
  }

}

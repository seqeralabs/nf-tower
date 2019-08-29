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

@Component({
  selector: 'wt-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss']
})
export class WelcomeComponent implements OnInit {

  user: User;

  nextflowRunCommand = 'nextflow run hello -with-tower ';

  constructor(private auth: AuthService) { }

  ngOnInit() {
    this.user = this.auth.currentUser
    const url = new URL(window.location.href);
    const base = url.origin;
    if( !base.endsWith('://tower.nf') ) {
      this.nextflowRunCommand += base + '/api'
    }
  }

}

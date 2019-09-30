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
import {AuthService} from "../../service/auth.service";
import {User} from "../../entity/user/user";

@Component({
  selector: 'wt-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {

  @Input()
  shouldShowSidebar: string;

  user: User;

  private isSidebarShown: boolean = true;

  constructor(private authService: AuthService) {

  }

  ngOnInit() {
    this.authService.user$.subscribe(
      (user: User) => this.user = user
    )
  }

  private toggleSidebar(): void {
    this.isSidebarShown = !this.isSidebarShown;
    console.log('navbar isSidebarShown = '+this.isSidebarShown);
  }
  get isSidebarToggled(): boolean {
    return this.isSidebarShown;
  }

}

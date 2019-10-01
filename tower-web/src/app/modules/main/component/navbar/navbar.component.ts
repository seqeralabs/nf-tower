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

import {Component, EventEmitter, Input, Output, OnInit} from '@angular/core';
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

  @Output()
  sidebarToggleEvent: EventEmitter<boolean> = new EventEmitter<boolean>();

  user: User;

  // Don't pay too much attention to the actual value -
  // shown/hidden behaviour alternates depending on window size
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
    this.sidebarToggleEvent.emit(this.isSidebarShown);
  }

}

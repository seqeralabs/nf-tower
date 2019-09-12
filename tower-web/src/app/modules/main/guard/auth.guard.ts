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
import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthService} from "../service/auth.service";
import {NotificationService} from "../service/notification.service";
import {Observable} from "rxjs";
import {tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService,
              private notificationService: NotificationService,
              private router: Router) {}

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Observable<boolean> {
    const isLoggedIn: boolean = this.checkLogin();
    if (!isLoggedIn) {
      return false;
    }

    return this.checkDisabled();
  }

  checkLogin(): boolean {
    if (this.authService.isUserAuthenticated) {
      return true;
    }

    console.log('User is not logged in');
    this.notificationService.showErrorNotification('Please log in');
    this.authService.logoutAndGoHome();
    return false;
  }

  checkDisabled(): Observable<boolean> {
    return this.authService.requestEnabledStatus().pipe(
      tap((isEnabled: boolean) => {
        if (isEnabled) {
          console.log('User is enabled');
          return;
        }

        console.log('User is disabled');
        this.notificationService.showErrorNotification('Your user has been disabled');
        this.authService.logoutAndGoHome();
      })
    );
  }

}

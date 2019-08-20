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
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {AuthService} from "../service/auth.service";
import {catchError} from "rxjs/operators";
import {Router} from "@angular/router";
import {NotificationService} from "../service/notification.service";

const authorizationErrorCodes: number[] = [401, 403];

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService,
              private notificationService: NotificationService,
              private router: Router) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError(error => {
        if (!authorizationErrorCodes.includes(error.status)) {
          return throwError(error);
        }

        console.log('Authorization error intercepted', error);
        if (this.authService.isUserAuthenticated && error.status == 401) {
          this.notificationService.showErrorNotification('Session expired');
          this.authService.logoutAndGoHome();
        } else if (error.status == 403) {
          this.notificationService.showErrorNotification('Forbidden access');
        }

        return throwError(error);
      })
    );

  }
}

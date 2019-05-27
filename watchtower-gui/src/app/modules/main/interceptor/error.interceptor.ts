import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {AuthService} from "../service/auth.service";
import {catchError} from "rxjs/operators";
import {Router} from "@angular/router";
import {NotificationService} from "../service/notification.service";

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private notificationService: NotificationService, private router: Router) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError(error => {
        const authorizationErrorCodes: number[] = [401, 403];

        if (authorizationErrorCodes.includes(error.status)) {
          console.log('Authorization error intercepted', error);
          if (this.authService.isUserAuthenticated && error.status == 401) {
            this.notificationService.showErrorNotification('Session expired');
            this.router.navigate(['/logout'])
          } else if (error.status == 403) {
            this.notificationService.showErrorNotification('Forbidden access');
          }
        }

        return throwError(error);
      })
    );

  }
}

import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, throwError} from 'rxjs';
import {AuthService} from "../service/auth.service";
import {catchError} from "rxjs/operators";
import {Router} from "@angular/router";

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private router: Router) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError(error => {
        let authorizationErrorCodes: number[] = [401, 403];

        if (authorizationErrorCodes.includes(error.status)) {
          this.authService.logout();
          this.router.navigate(['/logout'])
        }
        return throwError(error);
      })
    );

  }
}

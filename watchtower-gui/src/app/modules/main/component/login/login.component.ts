import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";
import {User} from "../../entity/user/user";
import {delay} from "rxjs/operators";

@Component({
  selector: 'wt-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  constructor(private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private notificationService: NotificationService) { }


  ngOnInit() {
    let queryParams: ParamMap = this.route.snapshot.queryParamMap;

    let email: string = queryParams.get('email');
    let authToken: string = queryParams.get('authToken');

    this.doAuth(email, authToken);
  }

  doAuth(email: string, authToken: string): void {
    console.log('Authenticating with', email, authToken);
    this.authService.login(email, authToken).pipe(
      delay(1500)
    ).subscribe(
      (user: User) => this.handleAuthenticationSuccess(user),
      (error: HttpErrorResponse) => this.handleAuthenticationError(error)
    )
  }

  private handleAuthenticationSuccess(user: User): void {
    console.log('User successfully authenticated', user);
    this.router.navigate(['']);
  }

  private handleAuthenticationError(error: HttpErrorResponse): void {
    let errorMessage: string = (error.status == 400) ? 'Bad credentials' :
                               (error.status == 401) ? 'Unauthorized'    : 'Unexpected error';

    this.notificationService.showErrorNotification(errorMessage);
  }

}

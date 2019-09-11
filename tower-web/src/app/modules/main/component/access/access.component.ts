import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";
import {NotificationService} from "../../service/notification.service";
import {User} from "../../entity/user/user";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'wt-access',
  templateUrl: './access.component.html',
  styleUrls: ['./access.component.scss']
})
export class AccessComponent implements OnInit {

  success: boolean;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private notificationService: NotificationService) {
  }

  ngOnInit() {
    const queryParams: ParamMap = this.route.snapshot.queryParamMap;
    this.success = (queryParams.get('success') == 'true');

    if (this.success) {
      this.authService.retrieveUser().subscribe(
        (user: User) => {
          console.log('User successfully authenticated', user);
          this.router.navigate(['']);
        },
        (resp: HttpErrorResponse) => {
          this.notificationService.showErrorNotification(`Login failed: ${resp.error.message}`);
        }
      );
    } else {
      this.notificationService.showErrorNotification('Login failed');
      this.router.navigate(['']);
    }
  }

}

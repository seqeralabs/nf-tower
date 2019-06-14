import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {Router} from "@angular/router";
import {NotificationService} from "../../service/notification.service";

@Component({
  selector: 'wt-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss']
})
export class LogoutComponent implements OnInit {

  constructor(private authService: AuthService,
              private router: Router
  ) { }

  ngOnInit() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

}

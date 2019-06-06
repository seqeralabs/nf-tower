import {Component, Input, OnInit} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {User} from "../../entity/user/user";

@Component({
  selector: 'wt-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {

  user: User;

  constructor(private authService: AuthService) {

  }

  ngOnInit() {
    this.authService.user$.subscribe(
      (user: User) => this.user = user
    )
  }


}

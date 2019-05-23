import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {User} from "../../entity/user/user";

@Component({
  selector: 'wt-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  title: string = 'seqeralabs';

  isUserLoggedIn: boolean;
  userAvatar: string;

  constructor(private authService: AuthService) {
    this.isUserLoggedIn = false;
  }

  ngOnInit() {
    this.authService.user$.subscribe(
      (user: User) => {
        this.isUserLoggedIn = true;
        this.userAvatar = user.avatar || 'https://upload.wikimedia.org/wikipedia/commons/7/7c/Profile_avatar_placeholder_large.png'
      }
    )
  }



}

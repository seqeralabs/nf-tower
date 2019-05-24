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

  user: User;

  constructor(private authService: AuthService) {

  }

  ngOnInit() {
    this.authService.user$.subscribe(
      (user: User) => this.user = user
    )
  }



}

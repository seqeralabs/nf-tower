import { Component } from '@angular/core';
import {AuthService} from "./service/auth.service";
import {User} from "./entity/user/user";

@Component({
  selector: 'wt-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent {

  user: User;

  constructor(private authService: AuthService) {

  }

  ngOnInit() {
    this.authService.user$.subscribe(
      (user: User) => this.user = user
    )
  }


}

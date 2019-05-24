import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../entity/user/user";

@Component({
  selector: 'wt-user-detail',
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss']
})
export class UserDetailComponent implements OnInit {

  @Input()
  user: User;

  constructor() { }

  ngOnInit() {
  }


  get userAvatar(): string {
    return this.user.avatar || 'https://upload.wikimedia.org/wikipedia/commons/7/7c/Profile_avatar_placeholder_large.png';
  }

}

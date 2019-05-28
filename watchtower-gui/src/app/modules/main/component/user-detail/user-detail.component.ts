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


  get nameToDisplay(): string {
    return (this.user.data.firstName && this.user.data.lastName) ? `${this.user.data.firstName} ${this.user.data.lastName}` : this.user.data.userName;
  }

}

import {Component, Input, OnInit} from '@angular/core';
import {User} from "../../entity/user/user";

@Component({
  selector: 'wt-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss']
})
export class WelcomeComponent implements OnInit {

  @Input()
  user: User;


  constructor() { }

  ngOnInit() {}

}

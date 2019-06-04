import { Component } from '@angular/core';
import {AuthService} from "./service/auth.service";

@Component({
  selector: 'wt-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent {

  isUserAuthenticated: boolean;

  constructor(private authService: AuthService) { }


  ngOnInit() {
    this.isUserAuthenticated = this.authService.isUserAuthenticated;
  }


}

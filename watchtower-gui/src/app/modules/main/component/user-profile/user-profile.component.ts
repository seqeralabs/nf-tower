import {Component, OnInit, ViewChild} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {User} from "../../entity/user/user";
import {NgForm} from "@angular/forms";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'wt-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {

  @ViewChild('profileForm')
  private profileForm: NgForm;
  private isSubmitted: boolean;

  userCopy: User;

  constructor(private authService: AuthService) { }

  ngOnInit() {
    this.userCopy = this.authService.currentUser.generateCopy();
    console.log("The user copy", this.userCopy);
  }

  update(): void {
    this.isSubmitted = true;
  }


  isSubmitEnabled(): boolean {
    return (!this.isSubmitted && this.profileForm.form.valid);
  }

}

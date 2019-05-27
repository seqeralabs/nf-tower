import {Component, OnInit, ViewChild} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {User} from "../../entity/user/user";
import {NgForm} from "@angular/forms";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";

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

  constructor(private authService: AuthService,
              private notificationService: NotificationService) { }

  ngOnInit() {
    this.authService.user$.subscribe(
      (user: User) => this.userCopy = user.generateCopy()
    );
  }

  update(): void {
    this.isSubmitted = true;
    this.authService.update(this.userCopy).subscribe(
      (message: string) => {
        this.notificationService.showSuccessNotification(message);
        this.isSubmitted = false;
      },
      (error: HttpErrorResponse) => {
        console.log('Error', error);
        if (error.status == 403) {
          this.notificationService.showErrorNotification(error.error);
        }
        this.isSubmitted = false;
      }
    )
  }


  isSubmitEnabled(): boolean {
    return (!this.isSubmitted && this.profileForm.form.valid);
  }

}

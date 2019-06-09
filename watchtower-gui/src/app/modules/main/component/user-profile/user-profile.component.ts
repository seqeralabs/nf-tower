import {Component, OnInit, ViewChild} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {User} from "../../entity/user/user";
import {NgForm} from "@angular/forms";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";
import {Router} from "@angular/router";

@Component({
  selector: 'wt-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {

  @ViewChild('profileForm')
  private profileForm: NgForm;

  userCopy: User;

  isSubmitted: boolean;
  confirmationDeleteEmail: string;

  constructor(private authService: AuthService,
              private notificationService: NotificationService,
              private router: Router) {
    this.confirmationDeleteEmail = '';
  }

  ngOnInit() {
    this.authService.user$.subscribe(
      (user: User) => this.userCopy = user.generateCopy()
    );
  }

  update(): void {
    this.isSubmitted = true;
    this.authService.update(this.userCopy).subscribe(
      (message: string) => this.handleOperationSuccess(message),
      (error: HttpErrorResponse) => this.handleOperationError(error)
    )
  }

  delete() {
    this.authService.delete().subscribe(
      (message: string) => {
        this.handleOperationSuccess(message);
        this.router.navigate(['/logout'])
      },
      (error: HttpErrorResponse) => this.handleOperationError(error)
    )
  }

  isSubmitEnabled(): boolean {
    return (!this.isSubmitted && this.profileForm.form.valid);
  }

  isUserDeletionEnabled(): boolean {
    const userEmail: string = this.authService.currentUser.data.email;

    return (this.confirmationDeleteEmail == userEmail);
  }

  private handleOperationSuccess(message: string): void {
    this.notificationService.showSuccessNotification(message);
    this.isSubmitted = false;
  }

  private handleOperationError(error: HttpErrorResponse): void {
    if (error.status == 400) {
      this.notificationService.showErrorNotification(error.error);
    }
    this.isSubmitted = false;
  }

}

/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */
import {Component, OnInit, ViewChild} from '@angular/core';
import {AuthService} from "src/app/modules/main/service/auth.service";
import {User} from "src/app/modules/main/entity/user/user";
import {NgForm} from "@angular/forms";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "src/app/modules/main/service/notification.service";
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

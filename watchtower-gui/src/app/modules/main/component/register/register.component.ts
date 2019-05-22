import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";
import {NgForm} from "@angular/forms";
import {NotificationService} from "../../service/notification.service";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
  selector: 'wt-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  @ViewChild('registerForm')
  private registerForm: NgForm;

  private email: string;
  private isSubmitted: boolean;

  private isRegistered: boolean;
  private registeredMessage: string;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private notificationService: NotificationService) {
    this.isSubmitted = false;
    this.isRegistered = false;
  }

  ngOnInit() {

  }

  submit(): void {
    this.isSubmitted = true;

    this.authService.register(this.email).subscribe(
      (message) => {
        this.isRegistered = true;
        this.registeredMessage = message;
      },
      (error: HttpErrorResponse) => {
        this.isSubmitted = false;
        this.notificationService.showErrorNotification(error.error);
      }
    );
  }

  isSubmitEnabled(): boolean {
    return (!this.isSubmitted && this.registerForm.form.valid);
  }

}

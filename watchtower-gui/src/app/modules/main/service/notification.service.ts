import { Injectable } from '@angular/core';
import {MzToastService} from "ngx-materialize";

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private toastService: MzToastService) { }

  showErrorNotification(errorMessage: string): void {
    this.showNotification(errorMessage, 'alert-error');
  }

  private showNotification(message: string, cssClass: string): void {
    this.toastService.show(message, 4000, cssClass);
  }
}

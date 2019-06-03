import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor() { }

  showSuccessNotification(message: string): void {
    // this.showNotification(message, 'alert-success');
  }

  showErrorNotification(errorMessage: string): void {
    // this.showNotification(errorMessage, 'alert-error');
  }

  private showNotification(message: string, cssClass: string): void {
    // this.toastService.show(message, 4000, cssClass);
  }
}

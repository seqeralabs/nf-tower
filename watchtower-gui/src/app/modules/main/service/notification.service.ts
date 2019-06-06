import {Injectable} from '@angular/core';
import {Observable, ReplaySubject, Subject} from "rxjs";
import {Notification} from "../entity/notification/notification";
import {NotificationType} from "../entity/notification/notification-type.enum";

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  notification$: Observable<Notification>;
  private notificationSubject: Subject<Notification>;

  constructor() {
    this.notificationSubject = new Subject();
    this.notification$ = this.notificationSubject.asObservable();
  }


  showSuccessNotification(message: string, msDelay: number = 3000): void {
    this.showNotification(new Notification(NotificationType.SUCCESS, message, msDelay));
  }

  showErrorNotification(message: string, msDelay: number = 3000): void {
    this.showNotification(new Notification(NotificationType.ERROR, message, msDelay));
  }

  private showNotification(notification: Notification): void {
    this.notificationSubject.next(notification);
  }
}

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


  showSuccessNotification(message: string, autohide: boolean = true, msDelay: number = 5000): void {
    this.showNotification(new Notification(NotificationType.SUCCESS, message, autohide, msDelay));
  }

  showErrorNotification(message: string, autohide: boolean = true, msDelay: number = 5000): void {
    this.showNotification(new Notification(NotificationType.ERROR, message, autohide, msDelay));
  }

  private showNotification(notification: Notification): void {
    this.notificationSubject.next(notification);
  }
}

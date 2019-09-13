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

import {Component, OnInit} from '@angular/core';
import {NotificationService} from "../../service/notification.service";
import {Notification} from "../../entity/notification/notification";
import {NavigationEnd, Router} from "@angular/router";


@Component({
  selector: 'wt-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {

  notificationsCache: Map<number, Notification>;
  notifications: Notification[];

  constructor(private notificationService: NotificationService,
              private router: Router) {
    this.notificationsCache = new Map();
  }

  ngOnInit() {
    this.notificationService.notification$.subscribe(
      (notification: Notification) => this.showNotification(notification)
    );
    this.clearNotificationsOnRouteChange();
  }

  private clearNotificationsOnRouteChange() {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.clearNotificationCache();
      }
    });
  }

  private showNotification(notification: Notification) {
    this.addNotificationToCache(notification);
    if (notification.autohide) {
      setTimeout(() => this.removeNotificationFromCache(notification), notification.msDelay);
    }
  }

  private addNotificationToCache(notification: Notification): void {
    this.notificationsCache.set(notification.id, notification);
    this.notifications = Array.from(this.notificationsCache.values());
  }

  private removeNotificationFromCache(notification: Notification): void {
    this.notificationsCache.delete(notification.id);
    this.notifications = Array.from(this.notificationsCache.values());
  }

  private clearNotificationCache(): void {
    this.notificationsCache.clear();
    this.notifications = [];
  }

}

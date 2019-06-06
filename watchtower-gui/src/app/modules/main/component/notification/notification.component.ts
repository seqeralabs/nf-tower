import {Component, OnInit} from '@angular/core';
import {NotificationService} from "../../service/notification.service";
import {Notification} from "../../entity/notification/notification";
import {NotificationType} from "../../entity/notification/notification-type.enum";

declare var $: any;

@Component({
  selector: 'wt-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {

  notificationsCache: Map<number, Notification>;
  notifications: Notification[];

  constructor(private notificationService: NotificationService) {
    this.notificationsCache = new Map();
  }

  ngOnInit() {
    this.notificationService.notification$.subscribe(
      (notification: Notification) => this.showNotification(notification)
    );
  }


  private showNotification(notification: Notification) {
    this.addNotificationToCache(notification);

    setTimeout(() => {
      $(`#notification-${notification.id}`)
        .toast({animation: false, delay: notification.msDelay}).toast('show')
        .on('hidden.bs.toast', () => this.removeNotificationFromCache(notification));
    });

  }

  private addNotificationToCache(notification: Notification) {
    this.notificationsCache.set(notification.id, notification);
    this.notifications = Array.from(this.notificationsCache.values());
    console.log('Added notif cache', this.notificationsCache);
  }

  private removeNotificationFromCache(notification: Notification) {
    this.notificationsCache.delete(notification.id);
    this.notifications = Array.from(this.notificationsCache.values());
    console.log('Removed notif from cache', this.notificationsCache);
  }

  getAlertClass(notification: Notification): string {
    return (notification.type == NotificationType.ERROR) ?   'danger'  :
           (notification.type == NotificationType.SUCCESS) ? 'success' : 'info'
  }

}

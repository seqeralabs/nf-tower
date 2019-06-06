import {NotificationType} from "./notification-type.enum";

export class Notification {

  static nextId: number = 1;

  id: number;
  type: NotificationType;
  message: string;
  msDelay: number;

  constructor(type: NotificationType, message: string, msDelay: number) {
    this.id = Notification.nextId++;
    this.type = type;
    this.message = message;
    this.msDelay = msDelay;
  }


  get isInfo() {
    return (this.type == NotificationType.INFO);
  }

  get isSuccess() {
    return (this.type == NotificationType.SUCCESS);
  }

  get isError() {
    return (this.type == NotificationType.ERROR);
  }

}

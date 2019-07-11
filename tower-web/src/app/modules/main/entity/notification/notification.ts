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
import {NotificationType} from "./notification-type.enum";

export class Notification {

  static nextId: number = 1;

  id: number;
  type: NotificationType;
  message: string;
  autohide: boolean;
  msDelay: number;

  constructor(type: NotificationType, message: string, autohide: boolean, msDelay: number) {
    this.id = Notification.nextId++;
    this.type = type;
    this.message = message;
    this.autohide = autohide;
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

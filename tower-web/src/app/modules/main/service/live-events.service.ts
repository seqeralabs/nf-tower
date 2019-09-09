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
import { Injectable } from '@angular/core';
import {environment} from "../../../../environments/environment";
import {BehaviorSubject, Observable, ReplaySubject, Subject, Subscriber} from "rxjs";
import {Workflow} from "../entity/workflow/workflow";
import {User} from "../entity/user/user";
import {LiveUpdate} from "../entity/live/live-update";
import {filter} from "rxjs/operators";

const endpointUrl: string = `${environment.apiUrl}/live`;

@Injectable({
  providedIn: 'root'
})
export class LiveEventsService {

  connectionStatus$: Observable<boolean>;
  private connectionStatusSubject: BehaviorSubject<boolean>;

  private events$: Observable<LiveUpdate>;

  constructor() {
    this.connectionStatusSubject = new BehaviorSubject(null);
    this.connectionStatus$ = this.connectionStatusSubject.asObservable();
  }

  connectToWorkflowEventsStream(workflow: Workflow): Observable<LiveUpdate> {
    this.connectToLiveStream();

    return this.events$.pipe(
      filter((event: LiveUpdate) => event.workflowId == workflow.data.id),
    )
  }

  connectToUserEventsStream(user: User): Observable<LiveUpdate> {
    this.connectToLiveStream();

    return this.events$.pipe(
      filter((event: LiveUpdate) => event.userId == user.data.id),
      filter((event: LiveUpdate) => event.isWorkflowUpdate)
    )
  }

  private connectToLiveStream(): void {
    if (this.events$) {
      return;
    }
    const sseUrl: string = `${endpointUrl}/`;
    this.events$ = this.connect(sseUrl);
  }

  private connect(url: string): Observable<LiveUpdate> {
    return new Observable((subscriber: Subscriber<LiveUpdate>) => {
      console.log('Connecting to receive live events', url);
      const eventSource: EventSource = new EventSource(url, { withCredentials: true });

      eventSource.addEventListener('open', () => {
        console.log('Connection established', new Date().toISOString());
        this.updateStatus(true);
      });

      eventSource.addEventListener('message', (event: MessageEvent) => {
        const dataArray: any[] = JSON.parse(event.data);
        console.log('Event', dataArray);
        if (!dataArray || (Array.isArray(dataArray) && dataArray.length == 0)) {
          return;
        }

        const events: LiveUpdate[] = dataArray.map((data) => new LiveUpdate(data));
        events.forEach((event: LiveUpdate) => {
          if (event.isError) {
            subscriber.error(event);
          } else {
            subscriber.next(event)
          }
        });

      });

      eventSource.addEventListener('error', () => {
        this.updateStatus(false);
        console.log('Event source error', new Date().toISOString());
      });
    });
  }

  private updateStatus(newStatus: boolean): void {
    const currentStatus: boolean = this.connectionStatusSubject.value;
    if (newStatus == currentStatus) {
      return;
    }

    this.connectionStatusSubject.next(newStatus);
  }

}

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
import {Observable, Subscriber} from "rxjs";
import {Workflow} from "../entity/workflow/workflow";
import {User} from "../entity/user/user";
import {SseEvent} from "../entity/sse/sse-event";
import {filter} from "rxjs/operators";

const endpointUrl: string = `${environment.apiUrl}/sse`;

@Injectable({
  providedIn: 'root'
})
export class ServerSentEventsService {

  private events$: Observable<SseEvent>;

  constructor() {
  }

  connectToWorkflowEventsStream(workflow: Workflow): Observable<SseEvent> {
    this.connectToLiveStream();

    return this.events$.pipe(
      filter((event: SseEvent) => event.workflowId == workflow.data.id),
    )
  }

  connectToUserEventsStream(user: User): Observable<SseEvent> {
    this.connectToLiveStream();

    return this.events$.pipe(
      filter((event: SseEvent) => event.userId == user.data.id),
      filter((event: SseEvent) => event.isWorkflow)
    )
  }

  private connectToLiveStream(): void {
    if (this.events$) {
      return;
    }
    const sseUrl: string = `${endpointUrl}/`;
    this.events$ = this.connect(sseUrl);
  }

  private connect(url: string): Observable<SseEvent> {
    return new Observable((subscriber: Subscriber<SseEvent>) => {
      console.log('Connecting to receive live events', url);

      const eventSource: EventSource = new EventSource(url);
      eventSource.addEventListener('message', (event: MessageEvent) => {
        const dataArray: any[] = JSON.parse(event.data);
        if (!dataArray || (Array.isArray(dataArray) && dataArray.length == 0)) {
          return;
        }

        const events: SseEvent[] = dataArray.map((data) => new SseEvent(data));
        events.forEach((event: SseEvent) => {
          if (event.error) {
            subscriber.error(event);
          } else {
            subscriber.next(event)
          }
        });

      });

      eventSource.addEventListener('error', () => {
        console.log('Event source error. Possible idle timeout', new Date().toISOString());
      });
    });
  }

}

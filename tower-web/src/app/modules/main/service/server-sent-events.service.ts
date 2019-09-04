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
import {Task} from "../entity/task/task";
import {Workflow} from "../entity/workflow/workflow";
import {SseError} from "../entity/sse/sse-error";
import {User} from "../entity/user/user";
import {SseHeartbeat} from "../entity/sse/sse-heartbeat";
import {Progress} from "../entity/progress/progress";

const endpointUrl: string = `${environment.apiUrl}/sse`;

@Injectable({
  providedIn: 'root'
})
export class ServerSentEventsService {

  constructor() { }

  connectToWorkflowLiveStream(workflow: Workflow): Observable<Workflow | Progress | SseHeartbeat | SseError> {
    const workflowDetailUrl: string = `${endpointUrl}/workflow/${workflow.id}`;

    return this.connect(workflowDetailUrl);
  }

  connectToUserLiveStream(user: User): Observable<Workflow | Progress | SseHeartbeat | SseError> {
    const workflowListUrl: string = `${endpointUrl}/user/${user.data.id}`;

    return this.connect(workflowListUrl);
  }

  private connect(url: string): Observable<Workflow | Progress | SseHeartbeat | SseError> {
    return new Observable((subscriber: Subscriber<Workflow | Progress>) => {
      console.log('Connecting to receive live events', url);

      const eventSource: EventSource = new EventSource(url);
      eventSource.addEventListener('message', (event: MessageEvent) => {
        const transformedData: any = this.transformEventData(JSON.parse(event.data));

        if (transformedData instanceof SseError) {
          subscriber.error(transformedData);
        } else {
          subscriber.next(transformedData);
        }
      });
      eventSource.addEventListener('error', () => {
        console.log('Event source error. Possible idle timeout', new Date().toISOString());
      });

      return () => {
        console.log('Disconnecting of live events', url);
        eventSource.close();
      };
    });
  }

  private transformEventData(data: any): Workflow | Progress | SseHeartbeat | SseError {
    if (data.workflow) {
      return new Workflow(data.workflow);
    }
    if (data.progress) {
      return new Progress(data.progress);
    }
    if (data.heartbeat) {
      return new SseHeartbeat(data.heartbeat);
    }
    if (data.error) {
      return new SseError(data.error);
    }
  }

}

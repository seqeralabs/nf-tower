import { Injectable } from '@angular/core';
import {environment} from "../../../../environments/environment";
import {Observable, Subscriber} from "rxjs";
import {Task} from "../entity/task/task";
import {Workflow} from "../entity/workflow/workflow";
import {SseError} from "../entity/sse/sse-error";
import {SseErrorType} from "../entity/sse/sse-error-type";
import {User} from "../entity/user/user";

const endpointUrl: string = `${environment.apiUrl}/trace/live`;

@Injectable({
  providedIn: 'root'
})
export class ServerSentEventsWorkflowService {

  constructor() { }

  connectToWorkflowDetailLive(workflow: Workflow): Observable<Workflow | Task | SseError> {
    const workflowDetailUrl: string = `${endpointUrl}/workflowDetail/${workflow.data.workflowId}`;

    return this.connect(workflowDetailUrl);
  }

  connectToWorkflowListLive(user: User): Observable<Workflow | Task | SseError> {
    const workflowListUrl: string = `${endpointUrl}/workflowList/${user.data.id}`;

    return this.connect(workflowListUrl);
  }

  private connect(url: string): Observable<Workflow | Task | SseError> {
    return new Observable((subscriber: Subscriber<Workflow | Task>) => {
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
        subscriber.error(new SseError({type: SseErrorType.UNEXPECTED, message: 'Unexpected SSE error happened'}));
      });

      return () => {
        console.log('Disconnecting of live events', url);
        eventSource.close();
      };
    });
  }

  private transformEventData(data: any): Workflow | Task | SseError {
    if (data.workflow) {
      return new Workflow(data.workflow);
    }
    if (data.task) {
      return new Task(data.task);
    }
    if (data.error) {
      return new SseError(data.error);
    }
  }

}

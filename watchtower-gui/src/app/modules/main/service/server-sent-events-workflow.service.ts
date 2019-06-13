import { Injectable } from '@angular/core';
import {environment} from "../../../../environments/environment";
import {Observable, Subscriber} from "rxjs";
import {Task} from "../entity/task/task";
import {Workflow} from "../entity/workflow/workflow";
import {SseError} from "../entity/sse/sse-error";
import {SseErrorType} from "../entity/sse/sse-error-type";

const endpointUrl: string = `${environment.apiUrl}/trace/live/workflowDetail`;

@Injectable({
  providedIn: 'root'
})
export class ServerSentEventsWorkflowService {

  constructor() { }

  connect(workflow: Workflow): Observable<Workflow | Task | any> {
    return new Observable((subscriber: Subscriber<Workflow | Task>) => {
      const workflowId: number | string = workflow.data.workflowId;

      console.log(`Connecting to receive live events from workflow ${workflowId}`);
      const eventSource: EventSource = new EventSource(`${endpointUrl}/${workflowId}`);

      eventSource.addEventListener('message', (event: MessageEvent) => {
        const transformedData: any = this.transformEventData(JSON.parse(event.data));

        if (transformedData instanceof SseError) {
          subscriber.error(transformedData);
        } else {
          subscriber.next(transformedData);
        }
      });
      eventSource.addEventListener('error', (event: MessageEvent) => {
        subscriber.error(new SseError({type: SseErrorType.UNEXPECTED, message: 'Unexpected SSE error happened'}));
      });

      return () => {
        console.log(`Disconnecting of live events from workflow: ${workflowId}`);
        eventSource.close();
      };
    });
  }

  private transformEventData(data: any): Workflow | Task | any {
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

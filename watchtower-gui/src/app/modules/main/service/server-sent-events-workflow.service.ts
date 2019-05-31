import { Injectable } from '@angular/core';
import {environment} from "../../../../environments/environment";
import {Observable, Subscriber} from "rxjs";
import {Task} from "../entity/task/task";
import {Workflow} from "../entity/workflow/workflow";

const endpointUrl: string = `${environment.apiUrl}/trace/live`;

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
        subscriber.next(this.transformEventData(JSON.parse(event.data)));
      });
      eventSource.addEventListener('error', (event: MessageEvent) => {
        subscriber.error('Error happened');
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
    return data;
  }

}

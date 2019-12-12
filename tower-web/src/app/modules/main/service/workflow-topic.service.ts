import {environment} from "../../../../environments/environment";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";
import {WorkflowTopic} from "../entity/workflowTag/workflow-topic";

const endpointUrl = `${environment.apiUrl}/tag`;

@Injectable({
  providedIn: 'root'
})
export class WorkflowTopicService {

  constructor(private http: HttpClient) {
  }

  getTopicList(workflowId: string): Observable<WorkflowTopic[]> {
    const url = `${endpointUrl}/list/${workflowId}`;

    return this.http.get(url).pipe(
      map((data: any) => data.workflowTags ? data.workflowTags.map((item: any) => new WorkflowTopic(item)) : []),
    );
  }

  saveTopicList(request: any): Observable<WorkflowTopic[]> {
    const url = `${endpointUrl}/save`;

    return this.http.post(url, request).pipe(
      map((response: any) => response.workflowTopics),
    );
  }

}

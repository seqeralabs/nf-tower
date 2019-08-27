import {environment} from "../../../../environments/environment";
import {Injectable} from "@angular/core";
import {FilteringParams} from "../util/filtering-params";
import {Observable, ReplaySubject} from "rxjs";
import {Workflow} from "../entity/workflow/workflow";
import {map, tap} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";
import {WorkflowTag} from "../entity/workflowTag/workflow-tag";

const endpointUrl = `${environment.apiUrl}/tag`;

@Injectable({
  providedIn: 'root'
})
export class WorkflowTagService {

  constructor(private http: HttpClient) {
  }

  getTagList(workflowId: number | string): Observable<WorkflowTag[]> {
    const url = `${endpointUrl}/list/${workflowId}`;

    return this.http.get(url).pipe(
      map((data: any) =>  data.workflowTags ? data.workflowTags.map((item: any) => new WorkflowTag(item)) : []),
    );
  }

  createTag(workflowId: number | string, workflowTag: WorkflowTag): Observable<WorkflowTag> {
    const url = `${endpointUrl}/create`;

    return this.http.post(url, {workflowId: workflowId, workflowTag: workflowTag.data}).pipe(
      map((data: any) => data.workflowTag ? new WorkflowTag(data.workflowTag) : null),
    );
  }

  deleteTag(workflowTag: WorkflowTag): Observable<any> {
    const url = `${endpointUrl}/${workflowTag.data.id}`;

    return this.http.delete(url);
  }

}

import {Injectable} from "@angular/core";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from "../../../../environments/environment";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CommentsService {

  private static readonly GET_COMMENTS_URL = `${environment.apiUrl}/workflow/{workflowId}/comments`;
  private static readonly PUT_COMMENT_BY_ID_URL = `${environment.apiUrl}/workflow/{workflowId}/comment`;
  private static readonly DELETE_COMMENT_BY_ID_URL = `${environment.apiUrl}/workflow/{workflowId}/comment`;
  private static readonly POST_COMMENT_ADD_BY_ID_URL = `${environment.apiUrl}/workflow/{workflowId}/comment/add`;

  constructor(private http: HttpClient) {
  }

  getCommentsByWorkflowId(workflowId: string): Observable<any> {
    return this.http.get<any>(CommentsService.GET_COMMENTS_URL
      .replace('{workflowId}', workflowId));
  }

  deleteWorkFlowCommentById(workflowId: string, request: any): Observable<any> {
    const httpOptions = {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      body: request
    };
    return this.http.delete<any>(CommentsService.DELETE_COMMENT_BY_ID_URL
        .replace('{workflowId}', workflowId.toString()),
      httpOptions)
  }

  saveWorkFlowComment(workflowId: string, request: any): Observable<any> {
    return this.http.post<any>(CommentsService.POST_COMMENT_ADD_BY_ID_URL
        .replace('{workflowId}', workflowId.toString()),
      {request})
  }

  updateWorkFlowCommentById(commentId: string, request: any): Observable<any> {
    return this.http.put<any>(CommentsService.PUT_COMMENT_BY_ID_URL
        .replace('{workflowId}', commentId.toString()),
      {request})
  }

}

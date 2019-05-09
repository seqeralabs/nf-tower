import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Workflow} from "../entity/workflow/workflow";
import {environment} from "../../../../environments/environment";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";


const endpointUrl: string = `${environment.apiUrl}/workflow`;

@Injectable({
  providedIn: 'root'
})
export class WorkflowService {

  constructor(private http: HttpClient) { }


  list(): Observable<Workflow[]> {
    const url: string = `${endpointUrl}/list`;

    return this.http.get(url).pipe(
      map((data: any[]) => data.map(item => new Workflow(item))),
    );
  }


}

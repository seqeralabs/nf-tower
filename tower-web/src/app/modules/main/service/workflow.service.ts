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
import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Workflow} from '../entity/workflow/workflow';
import {environment} from '../../../../environments/environment';
import {Observable, of, ReplaySubject, Subject} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {ProgressData} from '../entity/progress/progress-data';
import {FilteringParams} from "../util/filtering-params";
import {TaskData} from "../entity/task/task-data";


const endpointUrl = `${environment.apiUrl}/workflow`;

@Injectable({
  providedIn: 'root'
})
export class WorkflowService {

  private workflowsByIdCache: Map<string | number, Workflow>;
  private workflowsSubject: Subject<Workflow[]>;

  constructor(private http: HttpClient) {
    this.workflowsByIdCache = new Map();
    this.workflowsSubject = new ReplaySubject(1);
  }


  get workflows$(): Observable<Workflow[]> {
    if (this.isWorkflowsCacheEmpty()) {
      console.log('Initializing workflows');
      this.emitWorkflowsFromServer(new FilteringParams(30, 0, null));
    } else {
      console.log('Workflows already initialized');
      this.emitWorkflowsFromCache();
    }

    return this.workflowsSubject.asObservable();
  }

  emitWorkflowsFromServer(filteringParams: FilteringParams, clearCache: boolean = false): void {
    console.log('Emitting workflows from server [workflowsByIdCache, clearCache]', this.workflowsByIdCache, clearCache);
    this.requestWorkflowList(filteringParams, clearCache).subscribe((workflows: Workflow[]) => this.workflowsSubject.next(workflows));
  }

  private emitWorkflowsFromCache(): void {
    console.log('Emitting workflows from cache [workflowsByIdCache]', this.workflowsByIdCache);
    const cachedWorkflows: Workflow[] = Array.from(this.workflowsByIdCache.values());
    this.workflowsSubject.next(cachedWorkflows);
  }

  private requestWorkflowList(filteringParams: FilteringParams, clearCache: boolean): Observable<Workflow[]> {
    const url = `${endpointUrl}/list`;

    return this.http.get(url, { params: filteringParams.toHttpParams() }).pipe(
      map((data: any) => data.workflows ? data.workflows.map((item: any) => new Workflow(item)) : []),
      tap((workflows: Workflow[]) => {
        if (clearCache) {
          this.workflowsByIdCache.clear();
        }
        workflows.forEach((workflow: Workflow) => this.workflowsByIdCache.set(workflow.id, workflow));
      })
    );
  }

  getWorkflow(id: string | number, bypassCache: boolean = false): Observable<Workflow> {
    if (!bypassCache && !this.isWorkflowsCacheEmpty()) {
      console.log(`Getting workflow ${id} from cache`);
      const workflow: Workflow = this.workflowsByIdCache.get(id);
      if (workflow) {
        return of(workflow);
      }
    }

    return this.requestWorkflow(id);
  }

  private requestWorkflow(id: string | number): Observable<Workflow> {
    console.log(`Requesting workflow ${id}`);
    const url = `${endpointUrl}/${id}`;

    return this.http.get(url).pipe(
      map((data: any[]) => new Workflow(data)),
      tap((workflow: Workflow) => {
        const isAlreadyInCache: boolean = this.workflowsByIdCache.has(workflow.id);
        if (isAlreadyInCache) {
          this.workflowsByIdCache.set(workflow.id, workflow)
        }
      })
    );
  }

  getProgress(workflowId: string): Observable<ProgressData> {
    console.log(`Requesting progress for workflow ${workflowId}`);
    const url: string = `${endpointUrl}/${workflowId}/progress`;
    return this.http.get(url).pipe(
      map((data: any) => new ProgressData(data.progress))
    );
  }

  getTaskById(workflowId: string, taskId: string | number): Observable<TaskData> {
    const url: string = `${endpointUrl}/${workflowId}/task/${taskId}`;
    return this.http.get(url).pipe(
      map((data: any) => data.task)
    );
  }

  buildTasksGetUrl(workflowId: string): string {
    return `${endpointUrl}/${workflowId}/tasks`;
  }

  updateWorkflow(newWorkflow: Workflow): void {
    this.workflowsByIdCache.set(newWorkflow.id, newWorkflow);
    this.emitWorkflowsFromCache();
  }

  deleteWorkflow(workflow: Workflow): Observable<string> {
    const url = `${environment.apiUrl}/workflow/${workflow.id}`;
    return new Observable<string>( observer => {
      this.http.delete(url)
        .subscribe(
          resp => {
            this.workflowsByIdCache.delete(workflow.id);
            this.emitWorkflowsFromCache();
            observer.complete();
            },
          (resp: HttpErrorResponse) => observer.error(resp.error.message)
        );
    });
  }

  updateProgress(progress: ProgressData, workflow: Workflow): void {
    workflow.progress = progress;
  }

  private isWorkflowsCacheEmpty(): boolean {
    return (this.workflowsByIdCache.size === 0);
  }

}

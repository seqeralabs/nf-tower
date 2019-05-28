import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Workflow} from "../entity/workflow/workflow";
import {environment} from "../../../../environments/environment";
import {Observable, Subject, of, ReplaySubject} from "rxjs";
import {map, tap} from "rxjs/operators";
import {Task} from "../entity/task/task";


const endpointUrl: string = `${environment.apiUrl}/workflow`;

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
      this.requestWorkflowList().subscribe((workflows: Workflow[]) => this.workflowsSubject.next(workflows));
    } else {
      console.log('Getting workflows from cache');
      this.emitWorkflowsFromCache();
    }

    return this.workflowsSubject.asObservable();
  }

  private requestWorkflowList(): Observable<Workflow[]> {
    const url: string = `${endpointUrl}/list`;

    return this.http.get(url).pipe(
      map((data: any) => data.workflows.map((item: any) => new Workflow(item)).sort((w1: Workflow, w2: Workflow) =>  (w1.data.start < w2.data.start) ? 1 : ((w1.data.start > w2.data.start) ? -1 : 0))),
      tap((workflows: Workflow[]) => workflows.forEach((workflow: Workflow) => this.workflowsByIdCache.set(workflow.data.workflowId, workflow)))
    );
  }

  getWorkflow(id: string | number, bypassCache: boolean = false): Observable<Workflow> {
    if (!bypassCache && !this.isWorkflowsCacheEmpty()) {
      console.log(`Getting workflow ${id} from cache`);
      let workflow: Workflow = this.workflowsByIdCache.get(id);
      if (workflow) {
        return of(workflow);
      }
    }

    return this.requestWorkflow(id);
  }

  private requestWorkflow(id: string | number): Observable<Workflow> {
    console.log(`Requesting workflow ${id}`);
    const url: string = `${endpointUrl}/${id}`;

    return this.http.get(url).pipe(
      map((data: any[]) => new Workflow(data)),
      tap((workflow: Workflow) => this.workflowsByIdCache.set(workflow.data.workflowId, workflow))
    );
  }

  fetchTasks(workflow: Workflow): void {
    this.requestTasks(workflow).subscribe();
  }

  private requestTasks(workflow: Workflow): Observable<Task[]> {
    console.log(`Requesting tasks of workflow ${workflow.data.workflowId}`);
    const url: string = `${endpointUrl}/${workflow.data.workflowId}/tasks`;

    return this.http.get(url).pipe(
      map((data: any) => data.tasks.map((item) => new Task(item))),
      tap((tasks: Task[]) => workflow.tasks = tasks)
    );
  }

  updateWorkflow(workflow: Workflow): void {
    this.workflowsByIdCache.set(workflow.data.workflowId, workflow);
    this.emitWorkflowsFromCache();
  }

  private emitWorkflowsFromCache(): void {
    this.workflowsSubject.next(Array.from(this.workflowsByIdCache.values()));
  }

  private isWorkflowsCacheEmpty(): boolean {
    return (this.workflowsByIdCache.size === 0);
  }

}

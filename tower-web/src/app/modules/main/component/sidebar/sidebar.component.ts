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
import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Workflow} from "src/app/modules/main/entity/workflow/workflow";
import {WorkflowService} from "src/app/modules/main/service/workflow.service";
import {AuthService} from "src/app/modules/main/service/auth.service";
import {NotificationService} from "src/app/modules/main/service/notification.service";
import { ActivatedRoute, Router, NavigationEnd, Params } from '@angular/router';
import {debounceTime, distinctUntilChanged, filter} from 'rxjs/operators';
import {FormControl} from "@angular/forms";
import {FilteringParams} from "../../util/filtering-params";

@Component({
  selector: 'wt-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit, OnDestroy, OnChanges {

  @Input()
  workflows: Workflow[];

  @Output()
  onDeleteWorkflow: EventEmitter<Workflow> = new EventEmitter();
  @Output()
  onSearchingWorkflows: EventEmitter<string> = new EventEmitter();

  searchBoxFormControl: FormControl = new FormControl();
  offset: number = 0;
  isSearchTriggered: boolean;

  currentId: string | number;
  workflowToDelete: Workflow;



  constructor(private httpClient: HttpClient,
              private authService: AuthService,
              private workflowService: WorkflowService,
              private router: Router,
              private route: ActivatedRoute) {}


  ngOnInit() {
    if (this.router.url == '/') {
      this.goToFirstWorkflow();
    }

    this.currentId = this.route.snapshot.paramMap.get('id');
    // magic hack to get the current selected workflow id from the url params
    // https://github.com/angular/angular/issues/11023#issuecomment-399667101
    this.router.events.pipe(filter(event => event instanceof NavigationEnd))
      .subscribe( () => {
        let active = this.route;
        while (active.firstChild) { active = active.firstChild }
        active.params.subscribe( (params: Params) => {
          this.currentId = params['id'];
        });
      });

    this.subscribeToSearchTextInput();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes.workflows) {
      return;
    }

    if (this.workflowToDelete && this.workflowToDelete.data.workflowId == this.currentId) {
      this.workflowToDelete = null;
      this.goToFirstWorkflow()
    }

    this.isSearchTriggered = this.isSearchTriggered ? false : this.isSearchTriggered;
  }

  ngOnDestroy(): void {
    this.router.navigate(['/'])
  }

  private subscribeToSearchTextInput(): void {
    this.searchBoxFormControl.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe((text: string) => this.searchWorkflows(text));
  }

  private searchWorkflows(searchText: string): void {
    this.isSearchTriggered = true;
    this.onSearchingWorkflows.next(searchText);
  }

  private goToFirstWorkflow(): void {
    if (this.workflows.length > 0) {
      this.showWorkflowDetail(this.workflows[0]);
    } else {
      this.router.navigate(['/']);
    }
  }

  showWorkflowDetail(workflow: Workflow): void {
    this.router.navigate([`/workflow/${workflow.data.workflowId}`])
  }

  deleteWorkflow(workflow: Workflow): void {
    const confirm = prompt(`Please confirm the deletion of the workflow '${workflow.data.runName}' typing its name below (operation is not recoverable):`);
    if (confirm != workflow.data.runName) {
      return;
    }

    this.workflowToDelete = workflow;
    this.onDeleteWorkflow.next(workflow);
  }

}

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
import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Workflow} from "src/app/modules/main/entity/workflow/workflow";
import {WorkflowService} from "src/app/modules/main/service/workflow.service";
import {AuthService} from "src/app/modules/main/service/auth.service";
import {NotificationService} from "src/app/modules/main/service/notification.service";
import { ActivatedRoute, Router, NavigationEnd, Params } from '@angular/router';
import {debounceTime, distinctUntilChanged, filter} from 'rxjs/operators';
import {FormControl} from "@angular/forms";

@Component({
  selector: 'wt-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit, OnDestroy {

  @Input()
  workflows: Workflow[];

  searchBoxFormControl: FormControl;

  currentId: string | number;

  constructor(private httpClient: HttpClient,
              private notificationService: NotificationService,
              private authService: AuthService,
              private workflowService: WorkflowService,
              private router: Router,
              private route: ActivatedRoute) {
    this.searchBoxFormControl = new FormControl();
  }


  ngOnInit() {
    this.goToFirstWorkflow();
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

    this.searchBoxFormControl.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe((text: string) => {
      console.log('The text', text);
      this.workflowService.emitWorkflowsFromServer(null, 0, text);
    });
  }

  ngOnDestroy(): void {
    this.router.navigate(['/'])
  }

  private goToFirstWorkflow(): void {
    if (this.router.url == '/') {
      this.showWorkflowDetail(this.workflows[0]);
    }
  }

  showWorkflowDetail(workflow: Workflow): void {
    this.router.navigate([`/workflow/${workflow.data.workflowId}`])
  }

  deleteWorkflow(workflowToDelete: Workflow) {
    const confirm = prompt(`Please confirm the deletion of the workflow '${workflowToDelete.data.runName}' typing its name below (operation is not recoverable):`);
    if (confirm != workflowToDelete.data.runName) {
      return;
    }

    const oldWorkflowsSize: number = this.workflows.length;
    this.workflowService.deleteWorkflow(workflowToDelete).subscribe(
      () => {},
      (errorMessage: string) => this.notificationService.showErrorNotification(errorMessage),
      () => {
        if( workflowToDelete.data.workflowId != this.currentId ) {
          return;
        }

        setTimeout(() => {
          if (oldWorkflowsSize > 1) {
            this.showWorkflowDetail(this.workflows[0])
          }
        });
      }
    );
  }

}

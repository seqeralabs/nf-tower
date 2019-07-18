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
import {Component, Input, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Workflow} from "src/app/modules/main/entity/workflow/workflow";
import {WorkflowService} from "src/app/modules/main/service/workflow.service";
import {AuthService} from "src/app/modules/main/service/auth.service";
import {environment} from "src/environments/environment";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "src/app/modules/main/service/notification.service";
import { ActivatedRoute, Router, NavigationEnd, Params } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'wt-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  @Input()
  workflows: Workflow[];

  currentId;

  constructor(private httpClient: HttpClient,
              private notificationService: NotificationService,
              private authService: AuthService,
              private workflowService: WorkflowService,
              private router: Router,
              private route: ActivatedRoute) { }


  ngOnInit() {
    this.goToFirstWorkflow();
    this.currentId = this.route.snapshot.paramMap.get('id');
    // magic hack to get the current selected workflow id from the url params
    // https://github.com/angular/angular/issues/11023#issuecomment-399667101
    this.router.events.pipe(filter(event => event instanceof NavigationEnd))
      .subscribe( () => {
        let active = this.route;
        while (active.firstChild) { active = active.firstChild };
        active.params.subscribe( (params: Params) => {
          this.currentId = params['id'];
        });
      });
  }

  private goToFirstWorkflow(): void {
    if (this.router.url == '/') {
      this.showWorkflowDetail(this.workflows[0]);
    }
  }

  showWorkflowDetail(workflow: Workflow): void {
    this.router.navigate([`/workflow/${workflow.data.workflowId}`])
  }

  deleteWorkflowFromSidebar(workflow: Workflow) {
    let index = this.workflows.indexOf(workflow);
    if( index==-1 ) {
      console.log(`Oops... can't remove from sidebar workflow name=${workflow.data.runName} id=${workflow.data.workflowId}`)
      return
    }

    this.workflows.splice(index, 1);
    if( this.workflows.length == 0 ) {
      this.router.navigate([`/`]);
      return
    }

    // get current selected workflow id
    if( workflow.data.workflowId == this.currentId ) {
      this.showWorkflowDetail(this.workflows[0]);
    }
  }

}

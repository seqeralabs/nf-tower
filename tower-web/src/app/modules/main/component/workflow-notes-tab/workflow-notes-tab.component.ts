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

import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";
import {FormControl, Validators} from "@angular/forms";
import {WorkflowComment} from "../../entity/comment/workflow-comment";
import {AuthService} from "../../service/auth.service";
import {NoSpaceValidator} from "../../entity/no-space.validator";
import {Observable} from "rxjs";
import {environment} from "../../../../../environments/environment";
import {HttpClient} from "@angular/common/http";

const WORKFLOW_ENDPOINT = `${environment.apiUrl}/workflow`;

@Component({
  selector: 'wt-workflow-notes',
  templateUrl: './workflow-notes-tab.component.html',
  styleUrls: ['./workflow-notes-tab.component.scss']
})
export class WorkflowNotesTabComponent implements OnChanges {

  @Input()
  workflow: Workflow;

  allComments: Array<WorkflowComment> = [];
  commentTextFormControl: FormControl = new FormControl('', [
    Validators.required,
    Validators.minLength(3),
    Validators.maxLength(2048),
    NoSpaceValidator.noSpace,
    NoSpaceValidator.noNewLine
  ]);

  constructor(private http: HttpClient,
              private authService: AuthService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.loadWorkflowComments();
  }

  loadWorkflowComments() {
    this.getCommentsByWorkflowId(this.workflow.id).subscribe(value => {
      this.allComments = value.comments;
    });
  }

  getUserAvatar() {
    return this.authService.currentUser.avatar || '/assets/avatar_placeholder.png';
  }

  postWorkflowComment() {
    if (this.commentTextFormControl.valid) {
      this.saveWorkFlowComment(this.workflow.id, {
        text: this.commentTextFormControl.value.trim(),
        timestamp: new Date()
      }).subscribe(value => {
        this.allComments.push(value.comment);
      });
      this.commentTextFormControl.reset();
    }
  }

  editWorkflowComment(event): void {
    this.updateWorkFlowCommentById(this.workflow.id, event.updateData)
      .subscribe(() => {
        this.allComments = this.allComments.map(comment => comment.id === event.comment.id
          ? {...comment, text: event.updateData.text, dateCreated: event.updateData.timestamp} : comment
        );
      });
  }

  deleteWorkflowComment(commentId: number): void {
    const confirm = prompt(`Please confirm the note deletion typing 'YES' below (operation is not recoverable):`);
    if( confirm !== 'YES' )
      return;

    this.deleteWorkFlowCommentById(this.workflow.id, commentId)
      .subscribe(() => {
        this.allComments.map(comment => {
          if (comment.id === commentId) {
            return comment.deleted = true;
          }
        });
      });
  }

  private getCommentsByWorkflowId(workflowId: string): Observable<any> {
    return this.http.get<any>(`${WORKFLOW_ENDPOINT}/{workflowId}/comments`);
  }

  private deleteWorkFlowCommentById(workflowId: string, commentId: number): Observable<any> {
    return this.http.delete<any>(`${WORKFLOW_ENDPOINT}/${workflowId}/comment/${commentId}`);
  }

  private saveWorkFlowComment(workflowId: string, request: any): Observable<any> {
    return this.http.post<any>(`${WORKFLOW_ENDPOINT}/${workflowId}/comment/add`,{request});
  }

  private updateWorkFlowCommentById(workflowId: string, request: any): Observable<any> {
    return this.http.put<any>(`${WORKFLOW_ENDPOINT}/${workflowId}/comment`, {request});
  }

}

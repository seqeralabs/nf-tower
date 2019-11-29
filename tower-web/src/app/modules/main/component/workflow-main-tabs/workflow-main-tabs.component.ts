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
import {CommentsService} from "../../service/comments.services";
import {FormControl, Validators} from "@angular/forms";
import {WorkflowComment} from "../../entity/comment/workflow-comment";

@Component({
  selector: 'wt-workflow-main-tabs',
  templateUrl: './workflow-main-tabs.component.html',
  styleUrls: ['./workflow-main-tabs.component.scss']
})
export class WorkflowMainTabsComponent implements OnChanges {

  @Input()
  workflow: Workflow;
  allComments: Array<WorkflowComment> = [];
  commentForDelete: WorkflowComment;
  text: string;
  emptyField: string;
  invalid: boolean;
  successPush: boolean;
  showError: boolean;

  constructor(private commentsService: CommentsService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.loadWorkflowComments();
  }

  loadWorkflowComments() {
    this.commentsService.getCommentsByWorkflowId(this.workflow.id).subscribe(value => {
      this.allComments = value.comments;
    });
  }

  getTextFromEditor(value) {
    this.text = value.html;
  }
  getValid(value) {
    this.invalid = value;
  }

  postWorkflowComment() {
    if (this.invalid) {
      this.commentsService.saveWorkFlowComment(this.workflow.id, {
        text: this.text,
        timestamp: new Date()
      }).subscribe(value => {
        this.allComments.push(value.comment);
      });
      this.emptyField = ' ';
      this.successPush = true;
      this.showError = false;
    } else {
      this.showError = true;
    }
  }

  deleteWorkflowComment(): void {
    this.commentsService.deleteWorkFlowCommentById(this.workflow.id,
      {commentId: this.commentForDelete.id, timestamp: new Date()})
      .subscribe(() => {
        this.allComments.map(comment => {
          if (comment.id === this.commentForDelete.id)
            return comment.deleted = true;
        });
        this.commentForDelete = null;
      });
  }

}

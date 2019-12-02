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
  commentTextFormControl: FormControl = new FormControl('', [
    Validators.required,
    Validators.minLength(5),
    Validators.maxLength(2048)
  ]);

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

  postWorkflowComment() {
    if (this.commentTextFormControl.valid) {
      this.commentsService.saveWorkFlowComment(this.workflow.id, {
        text: this.commentTextFormControl.value,
        timestamp: new Date()
      }).subscribe(value => {
        this.allComments.push(value.comment);
      });
      this.commentTextFormControl.reset();
    }
  }

  editWorkflowComment(commentE): void {
    this.commentsService.updateWorkFlowCommentById(this.workflow.id, commentE.updateData)
      .subscribe(() => {
        this.allComments = this.allComments.map(comment => comment.id === commentE.comment.id
          ? {...comment, text: commentE.updateData.text, dateCreated: commentE.updateData.dateCreated} : comment
        );
      });
  }

  deleteWorkflowComment(): void {
    this.commentsService.deleteWorkFlowCommentById(this.workflow.id,
      {commentId: this.commentForDelete.id, timestamp: new Date()})
      .subscribe(() => {
        this.allComments.map(comment => {
          if (comment.id === this.commentForDelete.id) {
            return comment.deleted = true;
          }
        });
        this.commentForDelete = null;
      });
  }

}

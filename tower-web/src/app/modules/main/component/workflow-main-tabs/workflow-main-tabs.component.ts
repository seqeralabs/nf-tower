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

  readonly eventEdit: string = 'edit';
  readonly eventPost: string = 'post';

  @Input()
  workflow: Workflow;
  buttonEvent = this.eventPost;
  allComments: Array<WorkflowComment> = [];
  editComment: WorkflowComment; // for pass to HTML params in post Func
  commentTextFormControl: FormControl = new FormControl('', [
    Validators.required,
    Validators.minLength(5)
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

  doComment(commentId) {
    this.buttonEvent === this.eventPost ? this.postWorkflowComment() : this.editWorkflowComment(commentId)
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

  editWorkflowComment(commentId: number) {
    if (this.commentTextFormControl.valid) {
      let newText = this.commentTextFormControl.value;
      this.commentsService.updateWorkFlowCommentById(this.workflow.id, {
        commentId,
        text: newText,
        timestamp: new Date()
      }).subscribe(value => {
        this.allComments = this.allComments.map(comment => {
          if (comment.id === commentId) {
            comment.text = newText;
            comment.dateCreated = new Date();
          }
          return comment;
        })
      });
      this.buttonEvent = this.eventPost;
      this.commentTextFormControl.reset();
    }
  }

  deleteWorkflowComment(event: WorkflowComment): void {
    this.commentsService.deleteWorkFlowCommentById(this.workflow.id,
      {commentId: event.id, timestamp: new Date()})
      .subscribe(value => {
        this.allComments = this.allComments.filter(comment => {
          return comment.id !== event.id
        });
      });
  }

  getCommentForEdit(event: WorkflowComment): void {
    this.editComment = event;
    this.buttonEvent = this.eventEdit;
    this.commentTextFormControl.setValue(event.text);
  }

  cancelEditComment(): void {
    this.buttonEvent = this.eventPost;
    this.commentTextFormControl.reset();
  }

}

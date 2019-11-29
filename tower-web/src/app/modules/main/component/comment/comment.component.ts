import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {WorkflowComment} from "../../entity/comment/workflow-comment";
import {CommentsService} from "../../service/comments.services";
import {Workflow} from "../../entity/workflow/workflow";

@Component({
  selector: 'wt-comment',
  templateUrl: './comment.component.html',
  styleUrls: ['./comment.component.scss']
})
export class CommentComponent implements OnInit {

  readonly autoSizeForTextarea = 42;

  @Input() comment: WorkflowComment;
  @Input() workflow: Workflow;
  @Input() allComments: any[];
  @Output() editCommentOut = new EventEmitter();

  @Output() deleteCommentOut = new EventEmitter();
  showTextarea: boolean;
  invalidField: boolean;
  currentUser = this.userService.currentUser;
  html;
  successPush: boolean;
  showError: boolean;

  constructor(private userService: AuthService,
              private commentsService: CommentsService) {
  }

  ngOnInit() {
  }

  getText(value) {
    this.html = value.html;
  }

  getValid(value) {
    this.invalidField = value;
  }

  editComment(comment: WorkflowComment): void {
    if (this.invalidField) {
      this.showTextarea = false;
      this.editCommentOut.emit(comment);
      let newText = this.html;
      this.commentsService.updateWorkFlowCommentById(this.workflow.id, {
        commentId: this.comment.id,
        text: newText,
        timestamp: new Date()
      }).subscribe(value => {
        this.allComments = this.allComments.map(comment => {
          if (comment.id === this.comment.id) {
            comment.text = newText;
            comment.dateCreated = new Date();
          }
          return comment;
        });
      });
      this.successPush = true;
      this.showError = false;
    } else {
      this.showError = true;
    }
  }

  showTextAreaForEdit(): void {
    this.showTextarea = !this.showTextarea;
    this.html = this.comment.text;
  }

  chooseCommentForDelete(comment: WorkflowComment) {
    this.deleteCommentOut.emit(comment);
  }

  isOwnerOfComment(): boolean {
    let isOwner = false;
    if (this.comment) {
      isOwner = this.currentUser.data.userName === this.comment.author.displayName;
    }
    return isOwner;
  }

  getUserAvatar() {
    return this.comment.author.avatarUrl || '/assets/avatar_placeholder.png';
  }

}

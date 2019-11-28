import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {WorkflowComment} from "../../entity/comment/workflow-comment";
import {FormControl, Validators} from "@angular/forms";
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
  textareaRow: number;
  currentUser = this.userService.currentUser;
  commentTextEditFormControl: FormControl = new FormControl('', [
    Validators.required,
    Validators.minLength(5)
  ]);

  constructor(private userService: AuthService,
              private commentsService: CommentsService) {
  }

  ngOnInit() {
  }

  editComment(comment: WorkflowComment): void {
    if (this.commentTextEditFormControl.valid) {
      this.showTextarea = false;
      this.editCommentOut.emit(comment);
      let newText = this.commentTextEditFormControl.value;
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
        })
      });
      this.commentTextEditFormControl.reset();
    }
  }

  showTextAreaForEdit(): void {
    this.showTextarea = !this.showTextarea;
    this.commentTextEditFormControl.setValue(this.comment.text);
    this.textareaRow = this.commentTextEditFormControl.value.length / this.autoSizeForTextarea;
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

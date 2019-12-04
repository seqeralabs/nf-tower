import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {WorkflowComment} from "../../entity/comment/workflow-comment";
import {FormControl, Validators} from "@angular/forms";
import {Workflow} from "../../entity/workflow/workflow";
import {NoSpaceValidator} from "../../entity/no-space.validator";

@Component({
  selector: 'wt-workflow-note',
  templateUrl: './workflow-note.component.html',
  styleUrls: ['./workflow-note.component.scss']
})
export class WorkflowNoteComponent implements OnInit {

  readonly autoSizeForTextarea = 42;

  @Input() comment: WorkflowComment;
  @Input() workflow: Workflow;
  @Input() allComments: any[];
  @Output() editCommentOut = new EventEmitter();

  @Output() deleteCommentOut = new EventEmitter();

  textareaRow: number;
  showTextarea: boolean;
  currentUser = this.userService.currentUser;
  commentTextEditFormControl: FormControl = new FormControl('', [
    Validators.required,
    Validators.minLength(3),
    Validators.maxLength(2048),
    NoSpaceValidator.noSpace,
    NoSpaceValidator.noNewLine
  ]);

  constructor(private userService: AuthService) {
  }

  ngOnInit() {
  }

  editComment(comment: WorkflowComment): void {
    if (this.commentTextEditFormControl.valid) {
      this.showTextarea = false;
      const newText = this.commentTextEditFormControl.value;
      this.editCommentOut.emit({
        comment, updateData: {
          commentId: this.comment.id,
          text: newText.trim(),
          timestamp: new Date()
        }
      });
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

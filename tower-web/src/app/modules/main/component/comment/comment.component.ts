import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {WorkflowComment} from "../../entity/comment/workflow-comment";

@Component({
  selector: 'wt-comment',
  templateUrl: './comment.component.html',
  styleUrls: ['./comment.component.scss']
})
export class CommentComponent implements OnInit {

  @Input() comment: WorkflowComment;
  @Output() editCommentOut = new EventEmitter();
  @Output() deleteCommentOut = new EventEmitter();

  currentUser = this.userService.currentUser;

  constructor(private userService: AuthService) {
  }

  ngOnInit() {
  }

  editComment(comment: WorkflowComment): void {
    this.editCommentOut.emit(comment);
  }

  deleteComment(comment: WorkflowComment): void {
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
    let avatar = '/assets/avatar_placeholder.png';
    if (this.comment && this.comment.author && this.comment.author.avatarUrl) {
      avatar = this.comment.author.avatarUrl;
    }
    return avatar;
  }

}

import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  Renderer2,
  ViewChild
} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {WorkflowComment} from "../../entity/comment/workflow-comment";
import {FormControl, Validators} from "@angular/forms";
import {Workflow} from "../../entity/workflow/workflow";
import {NoSpaceValidator} from "../../entity/no-space.validator";

@Component({
  selector: 'wt-comment',
  templateUrl: './comment.component.html',
  styleUrls: ['./comment.component.scss']
})
export class CommentComponent implements OnInit {

  @Input() comment: WorkflowComment;
  @Input() workflow: Workflow;
  @Input() allComments: any[];
  @Output() editCommentOut = new EventEmitter();
  @Output() deleteCommentOut = new EventEmitter();

  private textareaElRef: ElementRef;

  @ViewChild('textarea', {static: false}) set textarea(textarea: ElementRef) {
    this.textareaElRef = textarea;
  }

  private preTextElRef: ElementRef;

  @ViewChild('preText', {static: false}) set preText(preText: ElementRef) {
    this.preTextElRef = preText;
  }

  showTextarea: boolean;
  currentUser = this.userService.currentUser;
  commentTextEditFormControl: FormControl = new FormControl('', [
    Validators.required,
    Validators.minLength(5),
    Validators.maxLength(2048),
    NoSpaceValidator.noSpace,
    NoSpaceValidator.noNewLine
  ]);

  constructor(private userService: AuthService,
              private renderer: Renderer2,
              private changeDetector: ChangeDetectorRef) {
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

  showHideTextAreaForEdit(): void {
    let heightPre: string;
    if (this.preTextElRef && this.preTextElRef.nativeElement) {
      heightPre = `${this.preTextElRef.nativeElement.offsetHeight}px`;
    }
    this.showTextarea = !this.showTextarea;
    this.commentTextEditFormControl.setValue(this.comment.text);
    this.changeDetector.detectChanges();
    if (this.textareaElRef && this.textareaElRef.nativeElement) {
      this.renderer.setStyle(this.textareaElRef.nativeElement, "height", heightPre);
    }
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

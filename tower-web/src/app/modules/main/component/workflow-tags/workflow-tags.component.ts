import {Component, Input, OnInit} from '@angular/core';
import {WorkflowTag} from "../../entity/workflowTag/workflow-tag";
import {WorkflowTagService} from "../../service/workflow-tag.service";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";
import {remove} from 'lodash';

@Component({
  selector: 'wt-workflow-tags',
  templateUrl: './workflow-tags.component.html',
  styleUrls: ['./workflow-tags.component.scss']
})
export class WorkflowTagsComponent implements OnInit {

  @Input()
  workflowId: number | string;

  tags: WorkflowTag[];

  isEditingTag: boolean;

  constructor(private workflowTagService: WorkflowTagService,
              private notificationService: NotificationService) {
  }

  ngOnInit() {
    this.workflowTagService.getTagList(this.workflowId).subscribe((tags: WorkflowTag[]) => {
      console.log('The tags', tags);
      this.tags = tags
    })
  }

  createNewTag() {
    const newTagText: string = this.generateNewTagText();

    this.persistNewTag(new WorkflowTag({text: newTagText}));
  }

  private generateNewTagText(): string {
    let newTagText: string = 'new-tag';

    let suffixNumber: number = this.tags.length + 1;
    let candidateText: string = `${newTagText}${suffixNumber}`;
    let isNameCollision;
    do {
      isNameCollision = this.tags.some((tag: WorkflowTag) => tag.data.text == candidateText);
      if (isNameCollision) {
        candidateText = `${newTagText}${++suffixNumber}`;
      } else {
        newTagText = candidateText;
      }
    } while (isNameCollision);

    return newTagText;
  }

  private persistNewTag(newWorkflowTag: WorkflowTag): void {
    this.workflowTagService.createTag(this.workflowId, newWorkflowTag).subscribe(
      (workflowTag: WorkflowTag) => {
      this.notificationService.showSuccessNotification('Tag successfully created');
      this.tags.push(workflowTag);
    },
      (httpError: HttpErrorResponse) => this.handleError(httpError)
    );
  }

  private handleError(httpError: HttpErrorResponse): void {
    if (httpError.status == 400) {
      this.notificationService.showErrorNotification(httpError.error.message)
    } else {
      this.notificationService.showErrorNotification('Unexpected error happened')
    }
  }

  deleteTag(workflowTagToDelete: WorkflowTag) {
    this.workflowTagService.deleteTag(workflowTagToDelete).subscribe(
      () => {
        this.notificationService.showSuccessNotification('Tag successfully deleted');
        remove(this.tags, (tag: WorkflowTag) => tag.data.id == workflowTagToDelete.data.id)
      },
    (httpError: HttpErrorResponse) => this.handleError(httpError)
    );
  }

}

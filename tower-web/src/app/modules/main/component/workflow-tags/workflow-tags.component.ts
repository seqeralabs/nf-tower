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
import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {WorkflowTag} from "../../entity/workflowTag/workflow-tag";
import {WorkflowTagService} from "../../service/workflow-tag.service";
import {HttpErrorResponse} from "@angular/common/http";
import {NotificationService} from "../../service/notification.service";
import {remove} from 'lodash';
import {Subject} from "rxjs";
import {debounceTime} from "rxjs/operators";

@Component({
  selector: 'wt-workflow-tags',
  templateUrl: './workflow-tags.component.html',
  styleUrls: ['./workflow-tags.component.scss']
})
export class WorkflowTagsComponent implements OnInit, OnChanges {

  @Input()
  workflowId: number | string;

  tags: WorkflowTag[];

  textEditionSubject: Subject<{tag: WorkflowTag, text: string}> = new Subject();

  constructor(private workflowTagService: WorkflowTagService,
              private notificationService: NotificationService) {
  }

  ngOnInit() {
    this.subscribeToTextEditionSubject();
  }

  ngOnChanges(): void {
    this.workflowTagService.getTagList(this.workflowId).subscribe((tags: WorkflowTag[]) => this.tags = tags);
  }


  private subscribeToTextEditionSubject() {
    this.textEditionSubject.pipe(
      debounceTime(500)
    ).subscribe((editPair: {tag: WorkflowTag, text: string}) => {
      const workflowTag: WorkflowTag = editPair.tag;
      workflowTag.data.text = editPair.text;
      this.updateTag(workflowTag);
    });
  }

  private updateTag(workflowTag: WorkflowTag): void {
    this.workflowTagService.updateTag(workflowTag).subscribe(
      () => {
        workflowTag.isValid = true;
        this.notificationService.showSuccessNotification('Tag successfully updated');
      },
      (httpError: HttpErrorResponse) => {
        workflowTag.isValid = false;
        this.handleError(httpError);
      }
    );
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
      (createdWorkflowTag: WorkflowTag) => {
      this.notificationService.showSuccessNotification('Tag successfully created');
      this.tags.push(createdWorkflowTag);
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
        remove(this.tags, (tag: WorkflowTag) => tag.data.id == workflowTagToDelete.data.id);
        this.notificationService.showSuccessNotification('Tag successfully deleted');
      },
    (httpError: HttpErrorResponse) => this.handleError(httpError)
    );
  }

  editTagText(workflowTagToEdit: WorkflowTag, text: string) {
    this.textEditionSubject.next({tag: workflowTagToEdit, text: text});
  }

}

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
import {Component, ElementRef, Input, OnChanges, OnInit, ViewChild} from '@angular/core';
import {WorkflowTopic} from "../../entity/workflowTag/workflow-topic";
import {WorkflowTopicService} from "../../service/workflow-topic.service";
import {Subject} from "rxjs";
import {debounceTime} from "rxjs/operators";
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'wt-workflow-topic',
  templateUrl: './workflow-topic.component.html',
  styleUrls: ['./workflow-topic.component.scss']
})
export class WorkflowTopicComponent implements OnInit, OnChanges {

  @Input()
  workflowId: string;

  @ViewChild('inputText', {static: false}) inputText: ElementRef;

  topics: WorkflowTopic[];
  editingTopics: WorkflowTopic[] = [];

  textEditionSubject: Subject<{ tag: WorkflowTopic, text: string }> = new Subject();

  showForm: boolean;
  formTopics: FormGroup;

  constructor(private workflowTopicService: WorkflowTopicService) {
  }

  ngOnInit() {
    this.formTopics = new FormGroup({
      topicText: new FormControl(null, [
        Validators.required,
        Validators.maxLength(35)
      ])
    });
    this.subscribeToTextEditionSubject();
    this.loadTopics();
  }

  ngOnChanges(): void {
    this.loadTopics();
  }

  loadTopics() {
    this.workflowTopicService.getTopicList(this.workflowId).subscribe((topics: WorkflowTopic[]) => {
      this.topics = topics;
    });
  }

  saveTopics() {
    this.workflowTopicService.saveTopicList(this.workflowId, {topics: this.editingTopics})
      .subscribe((topics: WorkflowTopic[]) => {
        this.topics = topics;
      });
    this.showForm = false;
    this.editingTopics = [];
  }

  private subscribeToTextEditionSubject() {
    this.textEditionSubject.pipe(
      debounceTime(500)
    ).subscribe((editPair: { tag: WorkflowTopic, text: string }) => {
      const workflowTag: WorkflowTopic = editPair.tag;
      workflowTag.data.text = editPair.text;
    });
  }

  addTag(event) {
    event.preventDefault();
    event.stopPropagation();
    const theSameTopic = this.editingTopics.some(t => t.data.text === this.formTopics.get('topicText').value);
    if (this.formTopics.valid && !theSameTopic && this.formTopics.get('topicText').value.trim()) {
      this.editingTopics.push(new WorkflowTopic({text: this.formTopics.get('topicText').value}));
      this.formTopics.reset();
    }
  }

  deleteTopic(event, topic) {
    this.editingTopics.splice(this.editingTopics.indexOf(topic), 1);
  }

  showFormTopics() {
    this.formTopics.reset();
    this.showForm = true;
    this.editingTopics = [...this.topics];
  }

  focusInput() {
    this.inputText.nativeElement.focus();
  }

}

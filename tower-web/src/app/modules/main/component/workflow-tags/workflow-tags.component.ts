import { Component, OnInit } from '@angular/core';
import {WorkflowTag} from "../../entity/workflow/workflow-tag";

@Component({
  selector: 'wt-workflow-tags',
  templateUrl: './workflow-tags.component.html',
  styleUrls: ['./workflow-tags.component.scss']
})
export class WorkflowTagsComponent implements OnInit {

  tags: WorkflowTag[];

  constructor() {
    this.tags = [<WorkflowTag>{label: 'seqera'}, <WorkflowTag>{label: 'workflow'}]
  }

  ngOnInit() {
  }

}

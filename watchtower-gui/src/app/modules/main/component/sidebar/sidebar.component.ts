import {Component, Input, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowService} from "../../service/workflow.service";
import {Router} from "@angular/router";

@Component({
  selector: 'wt-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  @Input()
  workflows: Workflow[];

  constructor(private workflowService: WorkflowService, private router: Router) { }

  ngOnInit() {
    this.showWorkflowDetail(this.workflows[0]);
  }


  showWorkflowDetail(workflow: Workflow): void {
    this.router.navigate([`/workflow/${workflow.data.workflowId}`])
  }
}

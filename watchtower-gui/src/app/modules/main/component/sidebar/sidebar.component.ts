import {Component, OnInit} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowService} from "../../service/workflow.service";
import {Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'wt-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  workflows: Workflow[];

  constructor(private authService: AuthService,
              private workflowService: WorkflowService,
              private router: Router) { }


  ngOnInit() {
    console.log('The router URL', this.router.url);
    this.initializeWorkflows();
  }

  private initializeWorkflows(): void {
    this.workflowService.workflows$.subscribe(
      (workflows: Workflow[]) => {
        this.workflows = workflows;

        if (this.router.url == '/' || this.router.url.startsWith('/auth')) {
          this.showWorkflowDetail(this.workflows[0]);
        }
      }
    )
  }

  showWorkflowDetail(workflow: Workflow): void {
    this.router.navigate([`/workflow/${workflow.data.workflowId}`])
  }
}

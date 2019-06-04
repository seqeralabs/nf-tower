import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {WorkflowService} from "../../service/workflow.service";
import {Workflow} from "../../entity/workflow/workflow";

@Component({
  selector: 'wt-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss']
})
export class WelcomeComponent implements OnInit {

  constructor() { }

  ngOnInit() {}

}

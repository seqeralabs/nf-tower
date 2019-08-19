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
import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";

declare let $: any;

@Component({
  selector: 'wt-workflow-main-tabs',
  templateUrl: './workflow-main-tabs.component.html',
  styleUrls: ['./workflow-main-tabs.component.scss']
})
export class WorkflowMainTabsComponent implements OnInit {

  @Input()
  workflow: Workflow;

  constructor() { }

  ngOnInit(): void {
    console.log(`workflow=${this.workflow.data}`)
  }


  ngOnChanges(changes: SimpleChanges): void {
    console.log(this.workflow.data.params)

    const params = [ {
      name: 'node1', id: 1,
      children: [
        { name: 'child1', id: 2 },
        { name: 'child2', id: 3 }
      ]
    } ];
    $(function() {
      $('#params-tree').tree({
        data: params
      });
    });
  }

}

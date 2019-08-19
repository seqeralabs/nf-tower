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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {Workflow} from "../../entity/workflow/workflow";

@Component({
  selector: 'wt-tree-list',
  templateUrl: './tree-list.component.html',
  styleUrls: ['./tree-list.component.scss']
})
export class TreeListComponent implements OnInit, OnChanges {

  @Input()
  workflow: Workflow;

  listContent: string;

  ngOnInit(): void { }

  ngOnChanges(changes: SimpleChanges): void {
    this.listContent = this.render(this.workflow.data.params);
  }

  private render(obj: any): string {
    if( obj instanceof Object ) {
      let html = '<ul>';
      for( const key in obj ) {
        html += `<li>${key}: ${this.render(obj[key])}</li>`;
      }
      return html + '</ul>';
    }

    if( obj instanceof Array ) {
      let html = '<ul>';
      for( const key in obj ) {
        html += `<li>${this.render(obj[key])}</li>`;
      }
      return html + '</ul>';
    }

    if( typeof(obj) == 'string' ) {
        return `"${obj}"`
    }
    else {
      return obj;
    }
  }

}

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
    const tree = this.render(this.workflow.data.params, 0);
    this.listContent = tree ? tree : '(no parameters)';
  }

  private render(obj: any, level:number): string {
    if( obj instanceof Object ) {
      let html = level>0 ? ':' : '';
      html += '<ul>';
      for( const key in obj ) {
        html += `<li><span class="list-label">${key}</span>${this.render(obj[key], level+1)}</li>`;
      }
      return html + '</ul>';
    }

    if( obj instanceof Array ) {
      let html = level>0 ? ':' : '';
      html += '<ul>';
      for( const key in obj ) {
        html += `<li>${this.render(obj[key], level+1)}</li>`;
      }
      return html + '</ul>';
    }

    return obj!=null ? ` = <span class="list-value">${obj}</span>` : '';
  }

}

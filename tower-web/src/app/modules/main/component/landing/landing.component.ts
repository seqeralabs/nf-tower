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

import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'wt-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements OnInit {

  @ViewChild('iframe')
  iframe: ElementRef;

  constructor() { }

  ngOnInit() {
  }

  /**
   * Set the height of the iframe to the height of its content in order to integrate the window scrollbar with the iframe content
   */
  resizeIFrame() {
    const contentIframeHeight: number = this.iframe.nativeElement.contentWindow.document.body.scrollHeight;
    this.iframe.nativeElement.height = contentIframeHeight;
  }

}

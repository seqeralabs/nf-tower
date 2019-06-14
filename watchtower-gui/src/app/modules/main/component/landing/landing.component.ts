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

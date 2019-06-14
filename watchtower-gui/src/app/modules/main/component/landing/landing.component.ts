import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'wt-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements OnInit {

  @ViewChild('landing')
  iframe: ElementRef;

  constructor() { }

  ngOnInit() {
  }

  resizeFrame() {
    const contentIframeHeight: number = this.iframe.nativeElement.contentWindow.document.body.scrollHeight;
    this.iframe.nativeElement.height = contentIframeHeight;
  }

}

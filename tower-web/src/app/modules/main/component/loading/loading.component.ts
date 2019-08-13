import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wt-loading',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.scss']
})
export class LoadingComponent implements OnInit {

  @Input()
  loadingText: string;

  constructor() { }

  ngOnInit() {
  }

}

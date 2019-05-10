import {Component, Input, OnInit} from '@angular/core';
import {Progress} from "../../entity/workflow/progress";

@Component({
  selector: 'wt-multicolored-progress-bar',
  templateUrl: './multicolored-progress-bar.component.html',
  styleUrls: ['./multicolored-progress-bar.component.scss']
})
export class MulticoloredProgressBarComponent implements OnInit {

  @Input()
  progress: Progress;


  constructor() { }

  ngOnInit() {
  }

}

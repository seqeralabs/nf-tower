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


  private total: number;

  constructor() { }

  ngOnInit() {
    this.computeTotal();
  }

  private computeTotal(): void {
    this.total = this.progress.running + this.progress.submitted + this.progress.cached + this.progress.failed + this.progress.pending + this.progress.succeeded;
  }

  private getPercentage(value: number): number {
    return value / this.total * 100;
  }


}

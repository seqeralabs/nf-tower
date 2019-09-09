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
import {AfterViewInit, Component, Input, OnChanges, OnInit} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowProgress} from "../../entity/progress/workflow-progress";

@Component({
  selector: 'wt-workflow-utilization',
  templateUrl: './workflow-utilization.component.html',
  styleUrls: ['./workflow-utilization.component.scss']
})
export class WorkflowUtilizationComponent implements OnInit, OnChanges {

  @Input()
  workflowProgress: WorkflowProgress;


  memoryDonutSeries: any;
  cpuDonutSeries: any;

  donutOptions: any = {
    donut: true, donutWidth: 10,
    labelInterpolationFnc: (value) => `${value}%`
  };

  donutEvents: any = {
    draw: this.centerTextInDonut
  };

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(): void {
    this.computeMemoryDonutSeries();
    this.computeCpuDonutSeries();
  }

  private centerTextInDonut(ctx): void {
    if (ctx.type != 'label') {
      return;
    }

    if(ctx.index === 0) {
      ctx.element.attr({dx: ctx.element.root().width() / 2, dy: ctx.element.root().height() / 2});
    } else {
      ctx.element.remove();
    }
  }

  private computeMemoryDonutSeries(): void {
    const memoryEfficiency: number = this.workflowProgress.data.memoryEfficiency;
    const unused = 100 - memoryEfficiency;

    this.memoryDonutSeries = this.computeDonutBinarySeries(memoryEfficiency, unused);
  }

  private computeCpuDonutSeries(): void {
    const cpuEfficiency: number = this.workflowProgress.data.cpuEfficiency;
    const unused = 100 - cpuEfficiency;

    this.cpuDonutSeries = this.computeDonutBinarySeries(cpuEfficiency, unused);
  }

  private computeDonutBinarySeries(filledValue: number, emptyValue: number): any[] {
    //Avoid phantom empty circle fragment when the empty value is a negative number (filled value exceeds 100%)
    emptyValue = (emptyValue < 0) ? 0 : emptyValue;

    return [
      {
        className: 'ct-filled',
        value: filledValue.toFixed(2)
      },
      {
        className: 'ct-empty',
        value: emptyValue.toFixed(2)
      }
    ];
  }

}

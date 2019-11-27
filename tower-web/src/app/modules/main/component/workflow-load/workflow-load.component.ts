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
import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";
import {WorkflowLoad} from "../../entity/progress/workflow-load";

@Component({
  selector: 'wt-workflow-load',
  templateUrl: './workflow-load.component.html',
  styleUrls: ['./workflow-load.component.scss']
})
export class WorkflowLoadComponent implements OnInit, OnChanges {


  @Input()
  workflowProgress: WorkflowLoad;

  coresGaugeSeries: any;
  tasksGaugeSeries: any;

  coreGaugeOptions: any;
  taskGaugeOptions; any;

  donutEvents: any = {
    draw: this.centerTextInGauge
  };

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(): void {
    console.log(`Gauge update loadCores=${this.workflowProgress.loadCpus}; maxCores=${this.workflowProgress.peakCpus}; loadTasks=${this.workflowProgress.loadTasks} maxTasks=${this.workflowProgress.peakTasks}`);
    this.coreGaugeOptions = this.computeGaugeOptions(this.workflowProgress.loadCpus, this.workflowProgress.peakCpus);
    this.taskGaugeOptions = this.computeGaugeOptions(this.workflowProgress.loadTasks, this.workflowProgress.peakTasks);
    this.coresGaugeSeries = this.computeGaugeBinarySeries(this.workflowProgress.loadCpus, this.workflowProgress.peakCpus);
    this.tasksGaugeSeries = this.computeGaugeBinarySeries(this.workflowProgress.loadTasks, this.workflowProgress.peakTasks);
  }

  private centerTextInGauge(ctx): void {
    if (ctx.type != 'label') {
      return;
    }

    if(ctx.index == 0) {
      ctx.element.attr({dx: ctx.element.root().width() / 2, dy: ctx.element.root().height() / 2});
    } else {
      ctx.element.remove();
    }
  }

  private computeGaugeOptions(filledValue: number, totalValue: number): any {
    //Avoid 50% fill bug when all values are 0
    totalValue = (totalValue == 0 && filledValue == 0) ? 1 : totalValue;

    return { donut: true, donutWidth: 10, startAngle: 270, total: totalValue * 2,
             labelInterpolationFnc: () => `${filledValue} / ${totalValue}`
    };
  }

  private computeGaugeBinarySeries(filledValue: number, totalValue: number): any[] {
    //Avoid 50% fill bug when all values are 0
    totalValue = (totalValue == 0 && filledValue == 0) ? 1 : totalValue;

    return [
      {
        className: 'ct-filled',
        value: filledValue
      },
      {
        className: 'ct-empty',
        value: totalValue-filledValue
      }
    ];
  }

}

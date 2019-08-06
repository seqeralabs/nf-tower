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
import {Component, Input, OnInit} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";

@Component({
  selector: 'wt-workflow-load',
  templateUrl: './workflow-load.component.html',
  styleUrls: ['./workflow-load.component.scss']
})
export class WorkflowLoadComponent implements OnInit {

  @Input()
  workflow: Workflow;

  coresGaugeSeries: any;
  tasksGaugeSeries: any;

  coreGaugeOptions: any;
  taskGaugeOptions; any;

  donutEvents: any = {
    draw: this.centerTextInGauge
  };

  constructor() { }

  ngOnInit() {
    const maxTasks: number = this.workflow.data.peakLoadTasks;
    const maxCores: number = this.workflow.data.peakLoadCpus;
    console.log(`Donut init maxCores=${maxCores}; maxTasks=${maxTasks}`);
    this.coreGaugeOptions = this.computeGaugeOptions(maxCores);
    this.taskGaugeOptions = this.computeGaugeOptions(maxTasks);
  }

  ngOnChanges(): void {
    const maxTasks: number = this.workflow.data.peakLoadTasks;
    const maxCores: number = this.workflow.data.peakLoadCpus;
    const loadTasks: number = this.workflow.progress.workflowProgress.loadTasks;
    const loadCores: number = this.workflow.progress.workflowProgress.loadCpus;
    console.log(`Donut update loadCores=${loadCores}; maxCores=${maxCores}; loadTasks=${loadTasks} maxTasks=${maxTasks}`);
    this.coreGaugeOptions = this.computeGaugeOptions(maxCores);
    this.taskGaugeOptions = this.computeGaugeOptions(maxTasks);
    this.coresGaugeSeries = this.computeGaugeBinarySeries(loadCores, maxCores);
    this.tasksGaugeSeries = this.computeGaugeBinarySeries(loadTasks, maxTasks);
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

  private computeGaugeOptions(total: number): any {
    return { donut: true, donutWidth: 10, startAngle: 270, total: total*2 };
  }

  private computeGaugeBinarySeries(filledValue: number, totalValue: number): any[] {
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

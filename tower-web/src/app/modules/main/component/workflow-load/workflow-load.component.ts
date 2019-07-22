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

  coreGaugeOptions: any = {
    donut: true, donutWidth: 10, startAngle: 270, total: 6000
  };
  taskGaugeOptions: any = {
    donut: true, donutWidth: 10, startAngle: 270, total: 6000
  };

  donutEvents: any = {
    draw: this.centerTextInGauge
  };

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(): void {
    this.computeCoreGaugeSeries();
    this.computeTaskGaugeSeries();
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

  private computeCoreGaugeSeries(): void {
    const cores: number = Math.floor(Math.random() * (this.coreGaugeOptions.total / 2));
    const unused = 3000 - cores;

    this.coresGaugeSeries = this.computeGaugeBinarySeries(cores, unused);
  }

  private computeTaskGaugeSeries(): void {
    const tasks: number = Math.floor(Math.random() * (this.taskGaugeOptions.total / 2));
    const unused = 3000 - tasks;

    this.tasksGaugeSeries = this.computeGaugeBinarySeries(tasks, unused);
  }

  private computeCoreGaugeOptions(): void {
    const total: number = Math.floor(Math.random() * 3000) * 2;
    this.coreGaugeOptions = {donut: true, donutWidth: 10, startAngle: 270, total: total}
  }

  private computeTasksGaugeOptions(): void {
    const total: number = Math.floor(Math.random() * 3000) * 2;
    this.taskGaugeOptions = {donut: true, donutWidth: 10, startAngle: 270, total: total}
  }

  private computeGaugeBinarySeries(filledValue: number, emptyValue: number): any[] {
    return [
      {
        className: 'ct-filled',
        value: filledValue
      },
      {
        className: 'ct-empty',
        value: emptyValue
      }
    ];
  }

}

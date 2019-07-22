import {AfterViewInit, Component, Input, OnChanges, OnInit} from '@angular/core';
import {Workflow} from "../../entity/workflow/workflow";

@Component({
  selector: 'wt-workflow-utilization',
  templateUrl: './workflow-utilization.component.html',
  styleUrls: ['./workflow-utilization.component.scss']
})
export class WorkflowUtilizationComponent implements OnInit, OnChanges {

  @Input()
  workflow: Workflow;


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
    const memoryEfficiency: number = Math.floor(Math.random() * 100);
    const unused = 100 - memoryEfficiency;

    this.memoryDonutSeries = this.computeDonutBinarySeries(memoryEfficiency, unused);
  }

  private computeCpuDonutSeries(): void {
    const cpuEfficiency: number = Math.floor(Math.random() * 100);
    const unused = 100 - cpuEfficiency;

    this.cpuDonutSeries = this.computeDonutBinarySeries(cpuEfficiency, unused);
  }

  private computeDonutBinarySeries(filledValue: number, emptyValue: number): any[] {
    return [
      {
        name: 'filled',
        className: 'ct-filled',
        value: filledValue
      },
      {
        name: 'empty',
        className: 'ct-empty',
        value: emptyValue
      }
    ];
  }

}

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
import {Component, OnInit} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {NotificationService} from 'src/app/modules/main/service/notification.service';
import {WorkflowMetrics} from 'src/app/modules/main/entity/workflow/workflow-metrics';
import {environment} from 'src/environments/environment';
import {ActivatedRoute} from '@angular/router';
import Plotly from './plotly-1.34.0.min.js';

declare let $: any;

@Component({
  selector: 'wt-workflow-metrics',
  templateUrl: './metrics.component.html',
  styleUrls: ['./metrics.component.scss']
})
export class WorkflowMetricsComponent implements OnInit {

  constructor(private httpClient: HttpClient,
              private route: ActivatedRoute,
              private notificationService: NotificationService) { }

  private data_byprocess = {};

  ngOnInit(): void {

    const workflowId = this.route.snapshot.paramMap.get('id');
    const url = `${environment.apiUrl}/workflow/${workflowId}/metrics`;
    this.httpClient.get<any>(url)
      .subscribe(
        data => {
            this.renderPlots(data.metrics); // <-- rename
        },
        (resp: HttpErrorResponse) => {
          this.notificationService.showErrorNotification(resp.error.message);
        }
      );
  }

  public renderPlots(allMetrics: Array<WorkflowMetrics>) {
    // Collect metrics by process
    for(let i in allMetrics){
      let metrics = allMetrics[i];
      let proc = metrics.process;

      if(!this.data_byprocess.hasOwnProperty(proc)){
        this.data_byprocess[proc] = {};
      }

      for (let key in metrics) {
        if (metrics[key] != null) {
          this.data_byprocess[proc][key] = [];
          if( metrics[key].min == metrics[key].max ) {
            // min equals max ==> show just a value
            this.data_byprocess[proc][key].push(metrics[key].min);
          }
          else {
            // otherwise show all values
            this.data_byprocess[proc][key].push(metrics[key].min);
            this.data_byprocess[proc][key].push(metrics[key].q1);
            this.data_byprocess[proc][key].push(metrics[key].q1);
            this.data_byprocess[proc][key].push(metrics[key].q2);
            this.data_byprocess[proc][key].push(metrics[key].q3);
            this.data_byprocess[proc][key].push(metrics[key].q3);
            this.data_byprocess[proc][key].push(metrics[key].max);
          }
          if (key == "time") {
            let x = this.time_min(this.data_byprocess[proc][key]);
            this.data_byprocess[proc][key] = x;
          }
        }
      }
    }

    // Plot histograms of resource usage
    let cpu_raw_data = [];
    let cpu_usage_data = [];
    let mem_raw_data = [];
    let mem_usage_data = [];
    let vmem_raw_data = [];
    let time_raw_data = [];
    let time_usage_data = [];
    let reads_raw_data = [];
    let writes_raw_data = [];
    for(let pname in this.data_byprocess){
      if( !this.data_byprocess.hasOwnProperty(pname) )
        continue;
      let smry = this.data_byprocess[pname];
      cpu_raw_data.push({y: smry.cpu, name: pname, type:'box', boxmean: true, boxpoints: false});
      cpu_usage_data.push({y: smry.cpuUsage, name: pname, type:'box', boxmean: true, boxpoints: false});
      mem_raw_data.push({y: this.norm_mem(smry.mem), name: pname, type:'box', boxmean: true, boxpoints: false});
      mem_usage_data.push({y: smry.memUsage, name: pname, type:'box', boxmean: true, boxpoints: false});
      vmem_raw_data.push({y: this.norm_mem(smry.vmem), name: pname, type:'box', boxmean: true, boxpoints: false});
      time_raw_data.push({y: smry.time, name: pname, type:'box', boxmean: true, boxpoints: false});
      time_usage_data.push({y: smry.timeUsage, name: pname, type:'box', boxmean: true, boxpoints: false});
      reads_raw_data.push({y: this.norm_mem(smry.reads), name: pname, type:'box', boxmean: true, boxpoints: false});
      writes_raw_data.push({y: this.norm_mem(smry.writes), name: pname, type:'box', boxmean: true, boxpoints: false});
    }

    Plotly.newPlot('cpuplot', cpu_raw_data, { title: 'CPU Usage', yaxis: {title: '% single core CPU usage', tickformat: '.1f', rangemode: 'tozero'} });
    Plotly.newPlot('memplot', mem_raw_data, { title: 'Physical Memory Usage', yaxis: {title: 'Memory', tickformat: '.4s', rangemode: 'tozero'} });
    Plotly.newPlot('timeplot', time_raw_data, { title: 'Task execution real-time', yaxis: {title: 'Execution time (minutes)', tickformat: '.1f', rangemode: 'tozero'} });
    Plotly.newPlot('readplot', reads_raw_data, { title: 'Number of bytes read', yaxis: {title: 'Read bytes', tickformat: '.4s', rangemode: 'tozero'} });

    // Only plot tabbed plots when shown
    $('#pctcpuplot_tablink').on('shown.bs.tab', function (e) {
      if($('#pctcpuplot').is(':empty')){
        Plotly.newPlot('pctcpuplot', cpu_usage_data, { title: '% Requested CPU Used', yaxis: {title: '% Allocated CPUs Used', tickformat: '.1f', rangemode: 'tozero'} });
      }
    });
    $('#pctmemplot_tablink').on('shown.bs.tab', function (e) {
      if($('#pctmemplot').is(':empty')){
        Plotly.newPlot('pctmemplot', mem_usage_data, { title: '% Requested Physical Memory Used', yaxis: {title: '% Memory', tickformat: '.1f', rangemode: 'tozero'} });
      }
    });
    $('#vmemplot_tablink').on('shown.bs.tab', function (e) {
      if($('#vmemplot').is(':empty')){
        Plotly.newPlot('vmemplot', vmem_raw_data, { title: 'Virtual Memory Usage', yaxis: {title: 'Memory', tickformat: '.4s', rangemode: 'tozero'} });
      }
    });
    $('#pcttimeplot_tablink').on('shown.bs.tab', function (e) {
      if($('#pcttimeplot').is(':empty')){
        Plotly.newPlot('pcttimeplot', time_usage_data, { title: '% Requested Time Used', yaxis: {title: '% Allocated Time Used', tickformat: '.1f', rangemode: 'tozero'} });
      }
    });
    $('#writeplot_tablink').on('shown.bs.tab', function (e) {
      if($('#writeplot').is(':empty')){
        Plotly.newPlot('writeplot', writes_raw_data, { title: 'Number of bytes written', yaxis: {title: 'Written bytes', tickformat: '.4s', rangemode: 'tozero'}});
      }
    });
  }

  /*
   * helper functions that takes an array of numbers each of each
   * is a integer representing a number of bytes and normalise to base 2 scale
   */
  norm_mem( list ) {
    if( list == null ) return null;
    let result = new Array(list.length);
    for( let i=0; i<list.length; i++ ) {
      let value = list[i];
      let x = Math.floor(Math.log10(value) / Math.log10(1024));
      if( x == 0 )
        value = value/1.024;
      else {
        for( let j=0; j<x; j++ )
          value = value / 1.024;
      }
      result[i] = Math.round(value);
    }
    return result;
  }

  private time_min(millis: number|Array<number>) {
    if( millis == null )
      return null;

    if( Array.isArray(millis) ) {
      let result = new Array(millis.length);
      for( let i in millis ) {
        result[i] = this.time_min0(millis[i]);
      }
      return result;
    }

    return this.time_min0(millis);
  }

  private time_min0(timeInMillis:number): number {
    return timeInMillis/1000/60;
  }
}

import {Component} from '@angular/core';
import {EChartsOption} from 'echarts';
import {AppStatusData} from '../../../model/app-status-data';

@Component({
  selector: 'receive-msg-total-chart',
  template: `
    <div style="width: 100%" echarts (chartInit)="echartsInstance = $event" [options]="chartOption"></div>
  `
})
export class ReceiveMsgTotalChartComponent {

  echartsInstance;

  chartOption: EChartsOption = {
    title: {
      text: '接收消息总量',
      left: '3%'
    },
    tooltip: {
      show: true,
      trigger: 'item',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      show: true,
      type: 'category',
      data: []
    },
    legend: {
      data: ['收到', '处理']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '收到',
        data: [],
        color: '#9b8bba',
        type: 'bar',
        barMaxWidth: 30
      },
      {
        name: '处理',
        data: [],
        color: '#61a0a8',
        type: 'bar',
        barMaxWidth: 30
      }
    ]
  };

  refreshChart = (appStatusData: AppStatusData) => {
    const receiveMap = appStatusData.msgReceivedCount;
    const processMap = appStatusData.msgProcessedCount;
    const namespacesSet = new Set(Object.keys(receiveMap).concat(Object.keys(processMap)));
    const receive = [];
    const process = [];
    const namespaces = [];
    for (const i of namespacesSet) {
      receive.push(receiveMap[i] ? receiveMap[i] : 0);
      process.push(processMap[i] ? processMap[i] : 0);
      namespaces.push(i);
    }
    if (this.echartsInstance) {
      this.echartsInstance.setOption({
          xAxis: {
            data: namespaces,
          },
          series: [{
            data: receive
          }, {
            data: process
          }]
        }
      );
    }
  };

}

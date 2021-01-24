import {Component} from '@angular/core';
import {EChartsOption} from 'echarts';
import {AppStatusData} from '../../../model/app-status-data';

@Component({
  selector: 'flow-statistics-chart',
  template: `
    <div echarts [options]="chartOption" (chartInit)="chartInstance = $event"></div>
  `
})
export class FlowStatisticsChartComponent {

  chartOption: EChartsOption = {
    title: {
      left: '3%',
      text: 'Flow数据'
    },
    tooltip: {
      show: true,
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['inflow', 'successful', 'failure']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01]
    },
    yAxis: {
      type: 'category',
      data: []
    },
    series: [
      {
        color: '#8fd3e8',
        name: 'inflow',
        type: 'bar',
        data: []
      },
      {
        color: '#9bca63',
        name: 'successful',
        type: 'bar',
        data: []
      },
      {
        color: '#fcce10',
        name: 'failure',
        type: 'bar',
        data: []
      }
    ]
  };

  chartInstance: any;


  refreshChart = (statusData: AppStatusData) => {

    const inflowMap = statusData.inflowCount;
    const successfulMap = statusData.successfulCount;
    const failureMap = statusData.failureCount;

    const flowNames = [];
    const inflowData = [];
    const successfulData = [];
    const failureData = [];

    for (const fName of new Set(Object.keys(inflowMap).concat(Object.keys(successfulMap)).concat(Object.keys(failureMap)))) {

      flowNames.push(fName);
      inflowData.push(inflowMap[fName] ? inflowMap[fName] : 0);
      successfulData.push(successfulMap[fName] ? successfulMap[fName] : 0);
      failureData.push(failureMap[fName] ? failureMap[fName] : 0);

    }

    if (this.chartInstance) {
      this.chartInstance.setOption(
        {
          yAxis: {
            data: flowNames
          },
          series: [
            {data: inflowData},
            {data: successfulData},
            {data: failureData}
          ]
        }
      );
    }


  }

}

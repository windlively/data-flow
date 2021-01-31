import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {EChartsOption} from 'echarts';
import {DataSourceService} from '../../../service/data-source.service';
import {AppService} from '../../../service/app.service';
import {AppStatusData} from '../../../model/app-status-data';

@Component({
  selector: 'core-chart-rate-chart',
  template: `
    <div echarts style="height: 400px" [options]="clusterRateChartOption" (chartInit)="echartsInstance = $event; echartsInstanceInit.emit($event)"></div>
  `
})
export class CoreRateChartComponent implements OnInit, OnDestroy {

  @Input('is-cluster')
  private isCluster: boolean = true;

  @Input('instance-name')
  private instanceName?: string;

  @Output("instance-init")
  public echartsInstanceInit: EventEmitter<any> = new EventEmitter<any>()

  public echartsInstance;

  constructor(public dataSource: DataSourceService,
              private app: AppService) {
  }

  // 集群的处理速率图表
  clusterRateChartOption: EChartsOption = {
    animation: true,
    title: {
      text: '处理速率',
      left: '3%'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['Flow处理速率', '消息处理速率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    toolbox: {
      feature: {
        saveAsImage: {}
      }
    },
    xAxis: {
      type: 'time',
      boundaryGap: false,
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'Flow处理速率',
        type: 'line',
        data: [],
        color: '#7cb4cc',
      },
      {
        name: '消息处理速率',
        type: 'line',
        data: [],
        color: '#9b8bba',
      }
    ]
  };

  ngOnInit(): void {
    this.refreshChart();
  }

  getTimeStr = (date: Date) => `${[date.getFullYear(), date.getMonth() + 1, date.getDate()].join('/')} ${date.getHours()}:${date.getMinutes()}:${date.getSeconds()}`;

  refreshChart = (currentData?: AppStatusData, lastData?: AppStatusData) => {

    const now = currentData ? new Date(currentData.timestamp) : new Date();
    const date = this.getTimeStr(now);

    if (!currentData) {

      const timestamp = now.getTime()

      const flowRateData = new Array(60);
      this.clusterRateChartOption.series[0].data = flowRateData;
      const msgRateData = new Array(60);
      this.clusterRateChartOption.series[1].data = msgRateData;

      for (let i = 0; i < 59; i++) {
        flowRateData.push([this.getTimeStr(new Date(timestamp - i * 1000)), 0]);
        msgRateData.push([this.getTimeStr(new Date(timestamp - i * 1000)), 0]);
      }

      return;

    }
    if (!!!lastData) {
      return;
    }

    const flowRateData: any[] = this.clusterRateChartOption.series[0].data;
    const msgRateData: any[] = this.clusterRateChartOption.series[1].data;

    flowRateData.push({
      name: date,
      value: [date, Object.values(currentData.successfulCount).reduce((s1, s2) => s1 + s2) - Object.values(lastData.successfulCount).reduce((s1, s2) => s1 + s2)]
    });

    flowRateData.shift();

    msgRateData.push({
      name: date,
      value: [date, Object.values(currentData.msgProcessedCount).reduce((s1, s2) => s1 + s2) - Object.values(lastData.msgProcessedCount).reduce((s1, s2) => s1 + s2)]
    });
    msgRateData.shift();

    // this.clusterRateChartOption.xAxis['data'].shift();
    // this.clusterRateChartOption.xAxis['data'].push(date)


    if (this.echartsInstance) {
      this.echartsInstance.setOption({
        series: [
          {data: flowRateData},
          {data: msgRateData}
        ]
      });
    }
  };

  ngOnDestroy(): void {
  }
}

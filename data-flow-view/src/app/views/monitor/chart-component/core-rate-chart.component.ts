import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {EChartsOption} from 'echarts';
import {interval, Subscription} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {DataSourceService} from '../../../service/data-source.service';
import {AppService} from '../../../service/app.service';

@Component({
  selector: 'core-chart-rate-chart',
  template: `
    <div echarts style="height: 400px" [options]="clusterRateChartOption" (chartInit)="echartsInstance = $event"></div>
  `
})
export class CoreRateChartComponent implements OnInit, OnDestroy{

  @Input("is-cluster")
  private isCluster: boolean = true;

  @Input('instance-name')
  private instanceName?: string;

  public echartsInstance;

  constructor(public dataSource: DataSourceService,
              private app: AppService) {}

  // 集群的处理速率图表
  clusterRateChartOption: EChartsOption = {
    title: {
      text: '处理速率'
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
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        smooth: true,
        name: 'Flow处理速率',
        type: 'line',
        data: []
      },
      {
        name: '消息处理速率',
        type: 'line',
        data: []
      }
    ]
  }

  ngOnInit(): void {
    this.startClusterRateChart();
  }

  private subscription: Subscription;

  startClusterRateChart = () => {
    this.refreshRateChartOption()

    let lastData = null;
    this.subscription = interval(1000).pipe(
      switchMap(() => this.dataSource.getClusterCountStatistics())
    ).subscribe(s => {
      this.refreshRateChartOption(s, lastData)
      lastData = s;
    }, e => {
      this.app.showSnackBar(e['message'])
    })
  }

  refreshRateChartOption = (currentData?, lastData?) => {

    if(!currentData){
      const xAxisData = new Array(60);
      xAxisData.fill("00:00:00")
      this.clusterRateChartOption.xAxis['data'] = xAxisData

      const flowRateData = new Array(60);
      flowRateData.fill(0)
      this.clusterRateChartOption.series[0].data = flowRateData

      const msgRateData = new Array(60);
      msgRateData.fill(0);
      this.clusterRateChartOption.series[1].data = msgRateData

      return;
    }

    if(!!!lastData) return

    const flowRateData: number[] = this.clusterRateChartOption.series[0].data;
    const msgRateData: number[] = this.clusterRateChartOption.series[1].data;


    console.log(currentData, lastData);
    flowRateData.shift();
    flowRateData.push(currentData['flow_successful'] - lastData['flow_successful'])

    msgRateData.shift();
    msgRateData.push(currentData['processed_msg'] - lastData['processed_msg'])

    this.clusterRateChartOption.xAxis['data'].shift();
    this.clusterRateChartOption.xAxis['data'].push(new Date().toDateString())


    if(this.echartsInstance) {
      this.echartsInstance.setOption(this.clusterRateChartOption);
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe()
  }
}

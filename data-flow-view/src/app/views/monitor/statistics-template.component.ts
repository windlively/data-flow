import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {interval, Subscription} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {AppService} from '../../service/app.service';
import {DataSourceService} from '../../service/data-source.service';
import {CoreRateChartComponent} from './chart-component/core-rate-chart.component';
import {ReceiveMsgTotalChartComponent} from './chart-component/receive-msg-total-chart.component';
import {FlowStatisticsChartComponent} from './chart-component/flow-statistics-chart.component';

@Component({
  selector: 'statistics-template',
  template: `
    <div>
      <core-chart-rate-chart #rateChart></core-chart-rate-chart>
    </div>
    <div>
      <receive-msg-total-chart #receiveMsgTotalChart></receive-msg-total-chart>
    </div>
    <div>
      <flow-statistics-chart #flowStatisticsChart></flow-statistics-chart>
    </div>
  `
})
export class StatisticsTemplateComponent implements OnInit, OnDestroy{

  constructor(public http: HttpClient,
              public app: AppService,
              public dataSource: DataSourceService) {
  }

  @ViewChild('rateChart')
  public rateChartComponent: CoreRateChartComponent;
  @ViewChild("receiveMsgTotalChart")
  public receiveMsgTotalChart: ReceiveMsgTotalChartComponent;
  @ViewChild('flowStatisticsChart')
  public flowStatisticChart: FlowStatisticsChartComponent;

  public interval: Subscription;

  ngOnInit(): void {

    let lastAppStatusData = null
    this.interval = interval(1000).pipe(
      switchMap(() => this.dataSource.getClusterAppStatusData())
    ).subscribe(s => {
      this.rateChartComponent.refreshRateChartOption(s, lastAppStatusData)
      this.receiveMsgTotalChart.refreshChart(s)
      this.flowStatisticChart.refreshChart(Object.assign(s))
      lastAppStatusData = s;
    }, e => {
      this.app.showSnackBar(e['message']);
    });

  }

  ngOnDestroy(): void {
    this.interval.unsubscribe();
  }





}

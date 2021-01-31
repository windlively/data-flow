import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {interval, Subscription, throwError} from 'rxjs';
import {catchError, map, switchMap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {AppService, wsServer} from '../../service/app.service';
import {DataSourceService} from '../../service/data-source.service';
import {CoreRateChartComponent} from './chart-component/core-rate-chart.component';
import {ReceiveMsgTotalChartComponent} from './chart-component/receive-msg-total-chart.component';
import {FlowStatisticsChartComponent} from './chart-component/flow-statistics-chart.component';
import {WebSocketSubject} from 'rxjs/internal-compatibility';
import {AppStatusData} from '../../model/app-status-data';

@Component({
  selector: 'statistics-template',
  template: `
    <div *ngIf="emptyData">
      暂无数据
    </div>
    <div>
      <div>
        <core-chart-rate-chart *ngIf="activeInstance" #rateChart></core-chart-rate-chart>
      </div>
      <div>
        <receive-msg-total-chart #receiveMsgTotalChart></receive-msg-total-chart>
      </div>
      <div>
        <flow-statistics-chart #flowStatisticsChart></flow-statistics-chart>
      </div>
    </div>

  `
})
export class StatisticsTemplateComponent implements OnInit, OnDestroy {

  constructor(public http: HttpClient,
              public app: AppService,
              public dataSource: DataSourceService) {
  }

  @ViewChild('rateChart')
  public rateChartComponent: CoreRateChartComponent;
  @ViewChild('receiveMsgTotalChart')
  public receiveMsgTotalChart: ReceiveMsgTotalChartComponent;
  @ViewChild('flowStatisticsChart')
  public flowStatisticChart: FlowStatisticsChartComponent;

  public interval: Subscription;

  @Input('instance-name')
  public instanceName: string = null;

  @Input('active-instance')
  public activeInstance: boolean = true;

  public emptyData = false;

  ngOnInit(): void {

    const url = `${wsServer}/monitor/status-data/${this.instanceName || '__all'}`;

    let lastAppStatusData = null;
    this.interval = new WebSocketSubject(url).pipe(
      catchError((err, caught) => {
        this.app.showSnackBar('ws connection failed!');
        return throwError(err);
      })
    ).subscribe((s: AppStatusData) => {

      if (AppStatusData.isEmpty(s)) {
        this.emptyData = true
        return
      }

      if (this.activeInstance) {
        this.rateChartComponent.refreshChart(s, lastAppStatusData);
      }

      this.receiveMsgTotalChart.refreshChart(s);
      this.flowStatisticChart.refreshChart(Object.assign(s));
      lastAppStatusData = s;
    });

  }

  ngOnDestroy(): void {
    this.interval.unsubscribe();
  }

}

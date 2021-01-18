import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DataSourceService {

  constructor(private http: HttpClient) {
  }

  public getAllFlowConfig(): Observable<any> {
    return this.http.get('/flow-config');
  }

  public getAllInstance(): Observable<any> {
    return this.http.get('monitor/instance');
  }

  // 集群下的流处理成功数量
  public getClusterFlowSuccessfulCount(): Observable<any> {
    return this.http.get('/monitor/cluster/flow-successful-count');
  }

  // 集群下的消息处理数量
  public getClusterMsgProcessedCount(): Observable<any> {
    return this.http.get('/monitor/cluster/msg-processed-count')
  }

  public getClusterCountStatistics(): Observable<{flow_successful: number, processed_msg}> {
    return this.http.get('/monitor/cluster/statistics', {params: {item: ['processed_msg', 'flow_successful']}})
  }

}

import {Injectable, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Subject} from 'rxjs';
import {FlowConfig} from './model/flow-config';

declare var monaco
@Injectable({
  providedIn: 'root'
})
export class AppService implements OnInit{

  public allFlowConfigList: FlowConfig[] = [];

  public allFlowConfigListSubject = new Subject<FlowConfig[]>();

  constructor(public http: HttpClient) {
    this.refreshAllFlowConfigList()
  }

  refreshAllFlowConfigList = () => {
    this.allFlowConfigList = []
    this.http.get('/assets/flow_config_example.json').subscribe((data: FlowConfig[]) => {
      for(let i = 0; i < 3; i ++){
        let k = 0
        for (let f of data){
          f = Object.assign({}, f)
          f._id =  `${f._id}for_test_${i * 10 + k}`
          this.allFlowConfigList.push(f)
          k ++
        }
      }
      this.allFlowConfigListSubject.next(this.allFlowConfigList)
    });
  }

  ngOnInit(): void {

  }

}

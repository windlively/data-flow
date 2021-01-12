import {Injectable, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Subject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AppService implements OnInit{

  public allFlowConfigList: Object[] = [];

  public allFlowConfigListSubject = new Subject();

  constructor(public http: HttpClient) {
    this.refreshAllFlowConfigList()
  }

  refreshAllFlowConfigList = () => {
    this.http.get('/assets/flow_config_example.json').subscribe((data: object[]) => {
      console.log(data);
      this.allFlowConfigList = data;
      this.allFlowConfigListSubject.next(this.allFlowConfigList)
    });
  }

  ngOnInit(): void {

  }

}

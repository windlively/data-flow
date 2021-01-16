import { Injectable } from '@angular/core';
import {AppService} from './app.service';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DataSourceService {

  constructor(private http: HttpClient) { }

  public getAllFlowConfig(): Observable<any>{
    return this.http.get('/flow-config')
  }

}

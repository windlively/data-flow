import {Injectable, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject, Observer, of} from 'rxjs';
import {FlowConfig} from './model/flow-config';
import {distinct, map} from 'rxjs/operators';
import {fromArray} from 'rxjs/internal/observable/fromArray';
import {FormControl, FormGroupDirective, NgForm} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';

declare var monaco
@Injectable({
  providedIn: 'root'
})
export class AppService implements OnInit{

  public allFlowConfigList: FlowConfig[] = [];

  public cache = {
    sourceList: [],
    schemaList: [],
    nameList: []
  }

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

      this.cache.sourceList = []
      this.cache.schemaList = []
      this.cache.nameList = []

      fromArray(this.allFlowConfigList).pipe(
        map((o) => o.source),
        distinct()
      ).subscribe(o => this.cache.sourceList.push(o))

      fromArray(this.allFlowConfigList).pipe(
        map((o) => o.schema),
        distinct()
      ).subscribe(o => this.cache.schemaList.push(o))

      fromArray(this.allFlowConfigList).pipe(
        map((o) => o.name),
        distinct()
      ).subscribe(o => this.cache.nameList.push(o))

      this.allFlowConfigListSubject.next(this.allFlowConfigList)
    });
  }

  ngOnInit(): void {

  }

}

/** Error when invalid control is dirty, touched, or submitted. */
export class AppErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const isSubmitted = form && form.submitted;
    return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
  }
}

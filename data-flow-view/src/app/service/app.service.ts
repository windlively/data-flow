import {Injectable, OnInit} from '@angular/core';
import {BehaviorSubject, Subject} from 'rxjs';
import {FlowConfig} from '../model/flow-config';
import {delay, distinct, map} from 'rxjs/operators';
import {fromArray} from 'rxjs/internal/observable/fromArray';
import {FormControl, FormGroupDirective, NgForm} from '@angular/forms';
import {ErrorStateMatcher} from '@angular/material/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DataSourceService} from './data-source.service';

@Injectable()
export class AppService implements OnInit{

  public cache = {
    sourceList: [],
    schemaList: [],
    nameList: []
  }

  public allFlowConfigList = new BehaviorSubject<FlowConfig[]>([]);

  public showLoadingBarSubject: Subject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(public dataSource: DataSourceService,
              public snackBar: MatSnackBar) {}

  refreshAllFlowConfigList = () => {

    this.dataSource.getAllFlowConfig().subscribe((data: FlowConfig[]) => {

      // for(let i = 0; i < 3; i ++){
      //   let k = 0
      //   for (let f of data){
      //     f = Object.assign({}, f)
      //     f._id =  `${f._id}for_test_${i * 10 + k}`
      //     this.allFlowConfigList.push(f)
      //     k ++
      //   }
      // }

      this.cache.sourceList = []
      this.cache.schemaList = []
      this.cache.nameList = []

      fromArray(data).pipe(
        map((o) => o.source),
        distinct()
      ).subscribe(o => this.cache.sourceList.push(o))

      fromArray(data).pipe(
        map((o) => o.schema),
        distinct()
      ).subscribe(o => this.cache.schemaList.push(o))

      fromArray(data).pipe(
        map((o) => o.name),
        distinct()
      ).subscribe(o => this.cache.nameList.push(o))

      this.allFlowConfigList.next(data)
    });
  }

  ngOnInit(): void {
    console.log("init service");
    this.refreshAllFlowConfigList()
  }

  public showSnackBar = (msg: string, config?: object) => {
    this.snackBar.open(msg, null, config ? config : {
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: 'snack-bar-container',
      duration: 2000
    })
  }

}

/** Error when invalid control is dirty, touched, or submitted. */
export class AppErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const isSubmitted = form && form.submitted;
    return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
  }
}

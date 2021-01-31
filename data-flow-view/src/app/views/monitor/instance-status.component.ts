import {Component, ComponentFactoryResolver, OnInit, TemplateRef, ViewChild, ViewContainerRef} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {StatisticsTemplateComponent} from './statistics-template.component';
import {combineAll} from 'rxjs/operators';

@Component({
  selector: 'instance-status',
  template: `
    <style>
      .instance-info-card {
        height: 100%;
        width: 100%;
        box-shadow: 0 1px 2px -2px rgb(0 0 0 / 16%), 0 3px 6px 0 rgb(0 0 0 / 12%), 0 5px 12px 4px rgb(0 0 0 / 9%);
        overflow-wrap: anywhere;
        padding: 10px;
        cursor: pointer;
      }
    </style>
    <div style="width: 100%; height: 100%; padding: 10px">
      <nz-row style="margin-bottom: 20px">
        <button mat-button (click)="getInstanceList()">刷新列表</button>
      </nz-row>
      <nz-row nzGutter="20">
        <nz-col style="margin-bottom: 20px" nzXXl="6" nzXl="8" nzMd="12" nzSm="24" *ngFor="let instance of instanceList">
          <div class="instance-info-card" matRipple (click)="showInstanceChart(instance.name, instance['active'])">
            <i nz-icon *ngIf="!!instance['active']">
              <svg t="1612097473212" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="20009">
                <path d="M512 42.666667a469.333333 469.333333 0 1 0 0 938.666666 469.333333 469.333333 0 0 0 0-938.666666z" fill="#47C55B"
                      p-id="20010"></path>
              </svg>
            </i>
            <i nz-icon *ngIf="!!!instance['active']">
              <svg t="1612097473212" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="20009">
                <path d="M512 42.666667a469.333333 469.333333 0 1 0 0 938.666666 469.333333 469.333333 0 0 0 0-938.666666z" fill="#cdcdcd"
                      p-id="20010"></path>
              </svg>
            </i>
            &nbsp;
            <span style="font-weight: bolder; font-size: 16px">{{instance.name}}</span>&nbsp;&nbsp;<span>({{instance['active'] ? '活动中':'离线'}})</span>
          </div>
        </nz-col>
      </nz-row>
      <div>
        <h2 style="text-align: center">{{selectedInstance}}</h2>
        <ng-template style="width: 100%; height: auto" #chart></ng-template>
      </div>
    </div>
  `
})
export class InstanceStatusComponent implements OnInit {

  @ViewChild("chart",  { read: ViewContainerRef })
  public chartContainer: ViewContainerRef

  instanceList: object[];

  private componentFactory = this.componentFactoryResolver.resolveComponentFactory(StatisticsTemplateComponent);

  constructor(private http: HttpClient,
              private componentFactoryResolver: ComponentFactoryResolver) {

  }

  ngOnInit(): void {
    this.getInstanceList();
  }

  getInstanceList = () => {
    this.http.get('/monitor/all-instance').subscribe((s: string[]) => {

      const instances: object[] = s.map(_ => {
        return {name: _};
      });

      this.http.get('/monitor/active-instance').subscribe((i: string[]) => {
        i.forEach(_ => {
          instances.find((v) => v['name'] === _)['active'] = true;
        });
        instances.sort((a1, a2) => (a1['active'] ? 0 : 1) - (a2['active'] ? 0 : 1))

        this.instanceList = instances;
      });
    });
  };


  showInstanceChart = (instanceName: string, active: boolean) => {
    this.chartContainer.clear()
    const ref = this.chartContainer.createComponent(this.componentFactory);
    const instance = ref.instance;
    instance.activeInstance = active;
    instance.instanceName = instanceName
    this.selectedInstance = instanceName
  }

  public selectedInstance: string = '';

}

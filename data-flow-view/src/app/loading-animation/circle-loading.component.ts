import {Component} from '@angular/core';

@Component(
  {
    selector: 'circle-loading',
    template: `
    <div style="width: 100%; height: 100%; min-height: 200px; display: flex; justify-content: center; align-items: center">
      <mat-spinner style="width: 60px"></mat-spinner>
    </div>
    `
  }
)
export class CircleLoadingComponent{

}

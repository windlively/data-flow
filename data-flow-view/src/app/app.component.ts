import {Component, OnInit} from '@angular/core';
import {AppService} from './service/app.service';
import {delay} from 'rxjs/operators';
import {animate, animateChild, group, query, stagger, style, transition, trigger} from '@angular/animations';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  animations: [

  ]
})
export class AppComponent implements OnInit {

  title = 'data-flow-view';
  routeLoading: boolean = true;


  constructor(public app: AppService) {
  }

  ngOnInit(): void {
    this.app.refreshAllFlowConfigList();
  }


}

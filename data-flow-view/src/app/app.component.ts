import {Component, OnInit} from '@angular/core';
import {AppService} from './service/app.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  standalone: false,
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

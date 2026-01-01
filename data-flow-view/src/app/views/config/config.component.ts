import {AfterViewInit, Component, OnInit} from '@angular/core';
import {AppService} from '../../service/app.service';
@Component({
    selector: 'app-config',
    templateUrl: './config.component.html',
    standalone: false,
    styleUrls: ['./config.component.css']
})
export class ConfigComponent implements OnInit, AfterViewInit {

  constructor(public app: AppService) { }

  ngOnInit(): void {

  }

  ngAfterViewInit(): void {
  }



}

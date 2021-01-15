import {Component, OnInit} from '@angular/core';
import {AppService} from './service/app.service';
import {delay} from 'rxjs/operators';
import {animate, animateChild, group, query, stagger, style, transition, trigger} from '@angular/animations';


export const routerTransition = trigger('routerTransition', [
    transition('* <=> *', [
      style({ position: 'relative' }),
      query(':enter, :leave', [
        style({
          position: 'absolute',
          top: 0,
          left: 0,
          width: '100%'
        })
      ]),
      query(':enter', [
        style({ left: '-100%' })
      ]),
      query(':leave', animateChild()),
      group([
        query(':leave', [
          animate('1000ms ease-out', style({ left: '100%' }))
        ]),
        query(':enter', [
          animate('1000ms ease-out', style({ left: '0%' }))
        ])
      ]),
      query(':enter', animateChild()),
    ]),
    // transition('* <=> FilterPage', [
    //   style({ position: 'relative' }),
    //   query(':enter, :leave', [
    //     style({
    //       position: 'absolute',
    //       top: 0,
    //       left: 0,
    //       width: '100%'
    //     })
    //   ]),
    //   query(':enter', [
    //     style({ left: '-100%' })
    //   ]),
    //   query(':leave', animateChild()),
    //   group([
    //     query(':leave', [
    //       animate('200ms ease-out', style({ left: '100%' }))
    //     ]),
    //     query(':enter', [
    //       animate('300ms ease-out', style({ left: '0%' }))
    //     ])
    //   ]),
    //   query(':enter', animateChild()),
    ]);


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  animations: [
    routerTransition
  ] // register the animations
})
export class AppComponent implements OnInit {

  title = 'data-flow-view';


  constructor(public app: AppService) {
  }

  ngOnInit(): void {
    this.app.refreshAllFlowConfigList();
  }

  getState(outlet) {
    return outlet && outlet.activatedRouteData && outlet.activatedRouteData['name']
  }

}

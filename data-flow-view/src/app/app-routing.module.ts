import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {FlowListComponent} from './views/flow-list/flow-list.component';
import {ConfigComponent} from './views/config/config.component';
import {MonitorComponent} from './views/monitor/monitor.component';


const routes: Routes = [
  {
    path: 'index',
    redirectTo: 'flow-list',
    pathMatch: 'full'
  },
  {
    path: 'flow-list',
    component: FlowListComponent,
    pathMatch: 'full',
    data: {
      name: 'flow-list'
    }
  },
  {
    path: 'config',
    component: ConfigComponent,
    pathMatch: 'full',
    data: {
      name: 'config'
    }
  },
  {
    path: 'monitor',
    component: MonitorComponent,
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'index',
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

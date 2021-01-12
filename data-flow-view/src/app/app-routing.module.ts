import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {FlowListComponent} from './flow-list/flow-list.component';
import {ConfigComponent} from './config/config.component';


const routes: Routes = [
  {
    path: 'index',
    redirectTo: 'flow-list',
    pathMatch: 'full'
  },
  {
    path: 'flow-list',
    component: FlowListComponent,
    pathMatch: 'full'
  },
  {
    path: 'config',
    component: ConfigComponent,
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

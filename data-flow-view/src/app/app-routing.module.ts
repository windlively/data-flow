import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {FlowListComponent} from './flow-list/flow-list.component';


const routes: Routes = [
  {
    path: 'flow-list',
    component: FlowListComponent,
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

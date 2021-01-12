import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {MatListModule} from "@angular/material/list";
import {RouterModule} from "@angular/router";
import {AppRoutingModule} from "./app-routing.module";
import {ToolButtonGroupComponent} from "./tool-button-group/tool-button-group.component";
import { NZ_I18N } from 'ng-zorro-antd/i18n';
import { zh_CN } from 'ng-zorro-antd/i18n';
import { registerLocaleData } from '@angular/common';
import zh from '@angular/common/locales/zh';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import {NzIconModule} from "ng-zorro-antd/icon";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";
import { FlowListComponent } from './flow-list/flow-list.component';
import {NgxEchartsModule} from 'ngx-echarts';
import "src/assets/echarts-theme/purple-passion.js";
import { ConfigComponent } from './config/config.component'
import {MatTabsModule} from '@angular/material/tabs';
import {AppService} from './app.service';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatTableModule} from "@angular/material/table";
import {MatInputModule} from "@angular/material/input";
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatCheckboxModule} from '@angular/material/checkbox';
registerLocaleData(zh);

@NgModule({
  declarations: [
    AppComponent,
    ToolButtonGroupComponent,
    FlowListComponent,
    ConfigComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MatListModule,
    RouterModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    NzIconModule,
    MatTooltipModule,
    MatButtonModule,
    MatDialogModule,
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    }),
    MatTabsModule,
    MatFormFieldModule,
    MatTableModule,
    MatInputModule,
    MatPaginatorModule,
    MatSortModule,
    MatCheckboxModule
  ],
  providers: [{ provide: NZ_I18N, useValue: zh_CN }, AppService],
  bootstrap: [AppComponent]
})
export class AppModule { }

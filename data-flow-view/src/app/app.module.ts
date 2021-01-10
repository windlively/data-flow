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

registerLocaleData(zh);

@NgModule({
  declarations: [
    AppComponent,
    ToolButtonGroupComponent
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
    MatDialogModule
  ],
  providers: [{ provide: NZ_I18N, useValue: zh_CN }],
  bootstrap: [AppComponent]
})
export class AppModule { }

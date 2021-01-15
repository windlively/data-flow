import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatListModule} from '@angular/material/list';
import {RouterModule} from '@angular/router';
import {AppRoutingModule} from './app-routing.module';
import {ToolButtonGroupComponent} from './tool-button-group/tool-button-group.component';
import {NZ_I18N} from 'ng-zorro-antd/i18n';
import {zh_CN} from 'ng-zorro-antd/i18n';
import {registerLocaleData} from '@angular/common';
import zh from '@angular/common/locales/zh';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatButtonModule} from '@angular/material/button';
import {MatDialogModule} from '@angular/material/dialog';
import {FlowListComponent} from './flow-list/flow-list.component';
import {NgxEchartsModule} from 'ngx-echarts';
import 'src/assets/echarts-theme/purple-passion.js';
import {ConfigComponent} from './config/config.component';
import {MatTabsModule} from '@angular/material/tabs';
import {AppService} from './service/app.service';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatTableModule} from '@angular/material/table';
import {MatInputModule} from '@angular/material/input';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {ErrorStateMatcher, MatOptionModule, MatRippleModule, ShowOnDirtyErrorStateMatcher} from '@angular/material/core';
import {PrettyJSONPipe} from './pipe/pretty-json.pipe';
import {FlowConfigComponent} from './config/flow-config/flow-config.component';
import {FlowConfigEditDialogComponent} from './config/flow-config/flow-config-edit-dialog.component';
import {MonacoEditorModule} from 'ngx-monaco-editor';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {ConfirmDialogComponent} from './dialog/confirm-dialog.component';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatProgressSpinnerModule, MatSpinner} from '@angular/material/progress-spinner';
import {DataFlowInterceptor} from './interceptor/data-flow.interceptor';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MAT_SNACK_BAR_DEFAULT_OPTIONS, MatSnackBarModule} from '@angular/material/snack-bar';
import {animate, group, query, stagger, style, transition, trigger} from '@angular/animations';

registerLocaleData(zh);


@NgModule({
  declarations: [
    AppComponent,
    ToolButtonGroupComponent,
    FlowListComponent,
    ConfigComponent,
    PrettyJSONPipe,
    FlowConfigComponent,
    FlowConfigEditDialogComponent,
    ConfirmDialogComponent
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
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    }),
    MatTabsModule,
    MatFormFieldModule,
    MatTableModule,
    MatInputModule,
    MatPaginatorModule,
    MatSortModule,
    MatCheckboxModule,
    MatButtonToggleModule,
    MatRippleModule,
    MonacoEditorModule.forRoot(),
    MatDialogModule,
    MatSlideToggleModule,
    MatOptionModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  providers: [
    AppService,
    {
      provide: NZ_I18N, useValue: zh_CN
    },
    {
      provide: HTTP_INTERCEPTORS, useClass: DataFlowInterceptor, multi: true
    },
    {
      provide: ErrorStateMatcher, useClass: ShowOnDirtyErrorStateMatcher
    },
    {
      provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {
        horizontalPosition: 'right',
        verticalPosition: 'top',
        duration: 2000
      }
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

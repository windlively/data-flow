import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {FlowConfig} from '../../../model/flow-config';
import IEditor = monaco.editor.IEditor;
import {ConfirmDialogComponent} from '../../../dialog/confirm-dialog.component';
import {map, startWith} from 'rxjs/operators';
import {FormControl, Validators} from '@angular/forms';
import {Observable} from 'rxjs';
import {AppErrorStateMatcher, AppService} from '../../../service/app.service';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-flow-config-edit-dialog',
  template: `
    <style>
      .mat-dialog-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px;
        margin-bottom: 0;
      }

      ngx-monaco-editor {
        height: calc(90vh - 240px);
        /*background-color: #ffffff00;*/
      }

      .monaco-editor {
        background-color: #9933ff;
      }

      .flow-namespace {
        margin-left: 10px;
        font-size: 16px;
      }

      .flow-config-meta-input-group {
        font-size: 16px;
      }

      .flow-config-meta-input {
        margin-right: 20px;
        width: 160px;
      }

      .flow-id-content h2{
        margin: 0;
      }

    </style>
    <div mat-dialog-title>
      <div [ngSwitch]="data.type" class="flow-id-content">
        <h2 *ngSwitchCase="'update'">{{currentFlowConfig._id}}</h2>
        <mat-form-field *ngSwitchCase="'new'" style="width: 300px;">
          <mat-label>_id</mat-label>
          <input matInput [formControl]="flowIdInputFormControl" (ngModelChange)="currentFlowConfig._id=$event">
          <mat-error *ngIf="flowIdInputFormControl.hasError('required')">
            必填
          </mat-error>
          <mat-error *ngIf="flowIdInputFormControl.hasError('pattern')">
            请输入字母、数字、下划线的组合(regex: \w+)
          </mat-error>
        </mat-form-field>
      </div>
      <div>
        <button mat-button style="margin-right: 20px" color="primary" (click)="save()"><i nz-icon nzType="save" nzTheme="outline"></i></button>
        <button mat-button color="primary" (click)="close()"><i nz-icon nzType="close" nzTheme="outline"></i></button>
      </div>
    </div>
    <div class="flow-config-meta-input-group">


      <mat-slide-toggle class="flow-config-meta-input" (change)="editorInstance.updateOptions({readOnly: $event.checked})">
        只读
      </mat-slide-toggle>

      <mat-form-field *ngFor="let input of inputList " class="flow-config-meta-input">
        <mat-label>{{input.name}}</mat-label>
        <input matInput
               [formControl]="input.formControl"
               (ngModelChange)="currentFlowConfig[input.name] = $event"
               [errorStateMatcher]="input.matcher"
               [matAutocomplete]="autocomplete">
        <mat-autocomplete autoActiveFirstOption #autocomplete="matAutocomplete">
          <mat-option *ngFor="let option of input.inputFilteredOptions | async" [value]="option">
            {{option}}
          </mat-option>
        </mat-autocomplete>
        <mat-error *ngIf="input.formControl.hasError('required')">
          必填
        </mat-error>
        <mat-error *ngIf="input.formControl.hasError('pattern')">
          请输入字母、数字、下划线的组合(regex: \w+)
        </mat-error>
      </mat-form-field>

    </div>
    <ngx-monaco-editor #editor id="flow-config-monaco-editor" style="" (onInit)="editorInit($event)" [options]="monacoEditorOption"
                       [(ngModel)]="editorContent">
      <circle-loading></circle-loading>
    </ngx-monaco-editor>
  `
})
export class FlowConfigEditDialogComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: {
                type: string,
                initFlowConfig: FlowConfig
              },
              public matDialogRef: MatDialogRef<FlowConfigEditDialogComponent>,
              public dialog: MatDialog,
              public app: AppService,
              public http: HttpClient) {

  }

  monacoEditorOption = {
    language: 'json',
    acceptSuggestionOnEnter: 'on',
    cursorBlinking: 'smooth',
    formatOnType: true,
    formatOnPaste: true,
    readOnly: false,
    smoothScrolling: true,
    cursorSmoothCaretAnimation: true
  };

  editorContent: string;

  // 复制一份配置对象，不影响原有实例
  currentFlowConfig: FlowConfig = JSON.parse(JSON.stringify(this.data.initFlowConfig));

  editorInstance: IEditor;

  flowIdInputFormControl = new FormControl('', [
    Validators.required,
    Validators.pattern(/^\w+$/)
  ]);

  inputList: {
    name: string,
    formControl: FormControl,
    inputFilteredOptions: Observable<string[]>,
    matcher: AppErrorStateMatcher
  }[] = [
    {
      name: 'source',
      formControl: null,
      inputFilteredOptions: null,
      matcher: new AppErrorStateMatcher()
    },
    {
      name: 'schema',
      formControl: null,
      inputFilteredOptions: null,
      matcher: new AppErrorStateMatcher()
    },
    {
      name: 'name',
      formControl: null,
      inputFilteredOptions: null,
      matcher: new AppErrorStateMatcher()
    }
  ];

  ngOnInit(): void {

    this.editorContent = JSON.stringify(this.currentFlowConfig, null, '\t');

    this.flowIdInputFormControl.setValue(this.currentFlowConfig._id, {
      emitModelToViewChange: true,
      emitViewToModelChange: true
    });
    for (const i of this.inputList) {
      const formControl = new FormControl('', [
        Validators.required,
        Validators.pattern(/^\w+$/)
      ]);
      i.formControl = formControl;
      formControl.setValue(this.currentFlowConfig[i.name], {
        emitViewToModelChange: true,
        emitModelToViewChange: true
      });
      i.inputFilteredOptions = formControl.valueChanges.pipe(
        startWith(''),
        map((value => this._filter(value, this.app.cache[`${i.name}List`])))
      );
    }
  }

  editorInit = (editor: IEditor) => {
    this.editorInstance = editor;
    // monaco.editor.defineTheme('my-theme', {
    //   base: 'vs',
    //   inherit: true,
    //   rules: [
    //     { token: '', background: '#00000000'}
    //   ],
    //   encodedTokensColors: [],
    //   colors: {
    //     'editor.background': '#00000000',
    //     'editor.lineHighlightBorder': '#8fd3e8',
    //     'minimap.background': '#00000000',
    //     "scrollbarSlider.background": "#0000001a",
    //     "minimapSlider.background": "#0000001a",
    //   }
    // });
    // monaco.editor.setTheme('my-theme');
  };


  private updateConfig = () => {
    for (const i of this.inputList) {
      if (!!!i.formControl.valid) {
        this.app.showSnackBar(i.formControl.errors.toString());
        return;
      }
    }

    try {
      const update: FlowConfig = JSON.parse(this.editorContent);
      this.currentFlowConfig.node_list = update.node_list;

      this.dialog.open(ConfirmDialogComponent, {
        width: '600px',
        data: {
          title: '确认更新配置：',
          msg: JSON.stringify(this.currentFlowConfig, null, '  ')
        }
      }).afterClosed().subscribe(s => {
        if(!!s){
          this.http.put('/flow-config', this.currentFlowConfig).subscribe(s => {
            this.app.showSnackBar(`更新${s}条配置`);
            this.app.refreshAllFlowConfigList();
            this.matDialogRef.close()
          })
        }else {
          this.app.showSnackBar('您取消了保存操作')
        }
      })

      ;
    } catch (e) {
      this.app.showSnackBar(e['message']);
    }
  };

  private addConfig = () => {
    if(!!!this.flowIdInputFormControl.valid){
      console.log(this.flowIdInputFormControl.errors)
      this.app.showSnackBar(`_id填写错误: ${JSON.stringify(this.flowIdInputFormControl.errors)}`);
      return;
    }
    for (const i of this.inputList) {
      if (!!!i.formControl.valid) {
        this.app.showSnackBar(`${i.name}填写错误: ${JSON.stringify(i.formControl.errors)}`);
        return;
      }
    }

    try {
      const flowConfig: FlowConfig = JSON.parse(this.editorContent);
      this.currentFlowConfig.node_list = flowConfig.node_list
      this.dialog.open(ConfirmDialogComponent, {
        width: '600px',
        data: {
          title: '确认新增配置：',
          msg: JSON.stringify(this.currentFlowConfig, null, '  ')
        }
      }).afterClosed().subscribe(s => {
        if(!!s) {
          this.http.post('/flow-config', this.currentFlowConfig).subscribe(s => {
            this.app.showSnackBar(`插入${s}条配置`);
            this.app.refreshAllFlowConfigList()
            this.matDialogRef.close()
          })
        }else {
          this.app.showSnackBar('您取消了保存操作')
        }
      })

    }catch (e) {
      this.app.showSnackBar(e['message'])
    }
  }

  close = () => {
    this.dialog.open(ConfirmDialogComponent, {
      // width: '400px',
      // height: '200px',
      data: {
        title: '确认关闭编辑器？',
        msg: '您已修改的内容将会丢失。'
      }
    }).afterClosed()
      .subscribe((data: boolean) => data ? this.matDialogRef.close() : null);
  };

  save = () => {
    switch (this.data.type) {
      case 'update':
        this.updateConfig();
        break;
      case 'new':
        this.addConfig();
        break;
    }
  };

  private _filter(value: string, options: string[]): string[] {
    const filterValue = value.toLowerCase();

    return options.filter(option => option.toLowerCase().includes(filterValue));
  }
}

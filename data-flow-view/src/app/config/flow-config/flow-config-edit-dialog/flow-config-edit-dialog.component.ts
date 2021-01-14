import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {FlowConfig} from '../../../model/flow-config';
import IEditor = monaco.editor.IEditor;

@Component({
  selector: 'app-flow-config-edit-dialog',
  template: `
    <style>
      .mat-dialog-title {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px;
      }

      ngx-monaco-editor {
        height: calc(90vh - 240px);
        /*background-color: #ffffff00;*/
      }

      .monaco-editor {
        background-color: #9933ff;
      }

      mat-dialog-container.mat-dialog-container {

      }

      .mat-dialog-container {

      }

      .flow-namespace {
        margin-left: 10px;
        font-size: 16px;
      }
      .flow-config-meta-input-group{
        font-size: 18px;
      }
      .flow-config-meta-input{
        margin-right: 20px;
        width: 160px;
      }
    </style>
    <div mat-dialog-title>
      <div class="flow-config-meta-input-group">
        <mat-form-field class="flow-config-meta-input" style="width: 300px;">
          <mat-label>_id</mat-label>
          <input matInput [(ngModel)]="currentFlowConfig._id">
        </mat-form-field>
        <mat-form-field class="flow-config-meta-input">
          <mat-label>source</mat-label>
          <input matInput [(ngModel)]="currentFlowConfig.source">
        </mat-form-field>
        <mat-form-field class="flow-config-meta-input">
          <mat-label>schema</mat-label>
          <input matInput [(ngModel)]="currentFlowConfig.schema">
        </mat-form-field>
        <mat-form-field class="flow-config-meta-input">
          <mat-label>name</mat-label>
          <input matInput [(ngModel)]="currentFlowConfig.name">
        </mat-form-field>

      </div>
      <div>
        <button mat-fab style="margin-right: 20px" color="primary"><i nz-icon nzType="save" nzTheme="outline"></i></button>
        <button mat-fab color="primary" mat-dialog-close><i nz-icon nzType="close" nzTheme="outline"></i></button>
      </div>
    </div>
    <div style="height: 60px">
      <mat-slide-toggle (change)="editorInstance.updateOptions({readOnly: $event.checked})">只读</mat-slide-toggle>
    </div>
    <ngx-monaco-editor #editor id="flow-config-monaco-editor" style="" (onInit)="editorInit($event)" [options]="monacoEditorOption"
                       [(ngModel)]="editorContent"></ngx-monaco-editor>
  `
})
export class FlowConfigEditDialogComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public initFlowConfig: FlowConfig) {

  }

  monacoEditorOption = {
    theme: 'my-theme',
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

  currentFlowConfig: FlowConfig;

  editorInstance: IEditor;

  ngOnInit(): void {

    // 复制一份配置对象，不影响原有实例
    this.currentFlowConfig = JSON.parse(JSON.stringify(this.initFlowConfig));

    // this.currentFlowConfig.source = undefined;
    // this.currentFlowConfig.schema = undefined;
    // this.currentFlowConfig.name = undefined;
    // this.currentFlowConfig._id = undefined;


    this.editorContent = JSON.stringify(this.currentFlowConfig, null, '\t');

  }

  editorInit = (editor: IEditor) => {
    this.editorInstance = editor;
    // monaco.editor.defineTheme('my-theme', {
    //   base: 'vs',
    //   inherit: true,
    //   rules: [
    //     { token: '', background: '#000000'}
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

}

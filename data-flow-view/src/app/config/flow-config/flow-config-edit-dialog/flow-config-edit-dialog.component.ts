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
        height: 60px;
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
    </style>
    <div mat-dialog-title>
      <div>
        {{initFlowConfig._id}}
      </div>
      <div>
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

  editorContent: string = JSON.stringify(this.initFlowConfig, null, '\t');

  editorInstance: IEditor;

  ngOnInit(): void {

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

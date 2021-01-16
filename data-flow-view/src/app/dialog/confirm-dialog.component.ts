import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-dialog',
  template: `
    <h2 mat-dialog-title>{{data && data['title'] || '确认'}}</h2>
    <div mat-dialog-content style="white-space: pre-wrap" *ngIf="data && data['msg']">
      {{data['msg']}}
    </div>
    <div mat-dialog-actions style="width: 100%; display: flex; justify-content: space-between">
      <button mat-button [mat-dialog-close]="false"><i nz-icon nzType="close" nzTheme="outline"></i></button>
      <button mat-button [mat-dialog-close]="true"><i nz-icon nzType="check" nzTheme="outline"></i></button>
    </div>
  `
})
export class ConfirmDialogComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: object,
              public matDialogRef: MatDialogRef<ConfirmDialogComponent>) { }

  ngOnInit(): void {
    this.matDialogRef.addPanelClass('blur-background')
  }

}

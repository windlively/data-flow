import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-dialog',
  template: `
    <h2 mat-dialog-title>{{data && data['title'] || '确认'}}</h2>
    <div mat-dialog-content *ngIf="data && data['msg']">
      {{data['msg']}}
    </div>
    <div mat-dialog-actions>
      <button mat-button [mat-dialog-close]="false" cdkFocusInitial>No</button>
      <button mat-button [mat-dialog-close]="true">Yes</button>
    </div>
  `
})
export class ConfirmDialogComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data: object,
              public matDialogRef: MatDialogRef<ConfirmDialogComponent>) { }

  ngOnInit(): void {

  }

}

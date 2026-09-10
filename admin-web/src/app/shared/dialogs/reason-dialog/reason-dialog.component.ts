import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface ReasonDialogData {
  title: string;
  label: string;
  confirmLabel: string;
}

@Component({
  selector: 'app-reason-dialog',
  standalone: true,
  imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" style="width: 100%">
        <mat-label>{{ data.label }}</mat-label>
        <textarea matInput rows="3" [(ngModel)]="reason" cdkFocusInitial></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Annuler</button>
      <button mat-flat-button color="primary" [disabled]="!reason.trim()" (click)="dialogRef.close(reason.trim())">
        {{ data.confirmLabel }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ReasonDialogComponent {
  reason = '';

  constructor(
    public dialogRef: MatDialogRef<ReasonDialogComponent, string | undefined>,
    @Inject(MAT_DIALOG_DATA) public data: ReasonDialogData,
  ) {}
}

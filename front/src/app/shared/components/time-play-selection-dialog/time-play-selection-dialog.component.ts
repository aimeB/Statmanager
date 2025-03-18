import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TimePlay, getTimePlayLabel } from '../../../shared/models/time-play.enum';
import { MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-time-play-selection-dialog',
  standalone: true,
  imports: [MatDialogModule, CommonModule], // ✅ Ajout ici
  template: `
    <h2 mat-dialog-title>⏳ Sélectionnez la minute du remplacement</h2>
    <mat-dialog-content class="timeplay-container">
      <button
        mat-raised-button
        *ngFor="let time of timePlayValues"
        (click)="selectionnerTimePlay(time)"
      >
        {{ getTimePlayLabel(time) }}
      </button>
    </mat-dialog-content>
  `,
  styles: [
    `
      .timeplay-container {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        justify-content: center;
      }
      button {
        min-width: 80px;
      }
    `,
  ],
})
export class TimePlaySelectionDialogComponent {
  timePlayValues: number[] = Object.values(TimePlay)
    .filter((value) => typeof value === 'number')
    .sort((a, b) => b - a) as number[];

  constructor(
    public dialogRef: MatDialogRef<TimePlaySelectionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {}

  getTimePlayLabel(value: number): string {
    return getTimePlayLabel(value as TimePlay);
  }

  selectionnerTimePlay(value: number) {
    this.dialogRef.close(value);
  }
}

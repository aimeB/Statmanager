import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FeuilleDeMatchDTO } from '../../../modules/feuille-de-match/models/feuille-de-match.model';
import { CommonModule } from '@angular/common'; // ✅ Import nécessaire
@Component({
  selector: 'app-selection-passeur-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>🎯 Sélectionner un passeur</h2>
    <div class="passeurs-list">
      <button
        *ngFor="let joueur of data.joueurs"
        class="btn-passeur"
        (click)="selectionner(joueur)"
      >
        {{ joueur.nom }} ({{ joueur.poste }})
      </button>
    </div>
    <button (click)="fermerSansPasseur()" class="btn-secondary">❌ Aucun passeur</button>
  `,
  styles: [
    `
      .passeurs-list {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
      }
      .btn-passeur {
        padding: 10px;
        border: none;
        cursor: pointer;
        background-color: #e0e0e0;
      }
      .btn-secondary {
        margin-top: 10px;
        background-color: #ff4d4d;
        color: white;
      }
    `,
  ],
})
export class SelectionPasseurDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<SelectionPasseurDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { joueurs: FeuilleDeMatchDTO[] },
  ) {}

  selectionner(joueur: FeuilleDeMatchDTO): void {
    console.log('🎯 Passeur sélectionné :', joueur);
    this.dialogRef.close(joueur);
  }

  fermerSansPasseur(): void {
    console.log('❌ Aucun passeur sélectionné.');
    this.dialogRef.close(null);
  }
}

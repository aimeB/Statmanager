import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common'; // ✅ Import nécessaire
import { JoueurDTO } from '../../models/joueur.model';
import { FeuilleDeMatchDTO } from '../../../feuille-de-match/models/feuille-de-match.model';

@Component({
  selector: 'app-joueur-selection-dialog',
  standalone: true,
  imports: [CommonModule, MatListModule, MatIconModule],
  template: `
    <h2>🔄 Choisir un joueur</h2>
    <mat-list>
      <mat-list-item *ngFor="let joueur of data.joueurs" (click)="selectionnerJoueur(joueur)">
        {{ joueur.nom }} - {{ joueur.poste }}
      </mat-list-item>
    </mat-list>
  `,
  styles: [
    `
      mat-list-item {
        cursor: pointer;
        padding: 10px;
        border-bottom: 1px solid #ccc;
      }
      mat-list-item:hover {
        background-color: #e0f7fa;
      }
    `,
  ],
})
export class JoueurSelectionDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<JoueurSelectionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { joueurs: JoueurDTO[] },
  ) {}

  selectionnerJoueur(joueur: JoueurDTO): void {
    console.log('🔄 Joueur sélectionné dans le popup :', joueur);
    this.dialogRef.close(joueur); // ✅ Renvoie le joueur sélectionné
  }

  fermerSansPasseur(): void {
    console.log('❌ Aucun passeur sélectionné.');
    this.dialogRef.close(null); // ✅ Renvoie `null` au composant parent
  }
}

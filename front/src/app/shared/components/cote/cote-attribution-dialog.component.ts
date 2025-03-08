import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FeuilleDeMatchDTO } from '../../../../app/modules/feuille-de-match/models/feuille-de-match.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cote-attribution-dialog',
  standalone: true,
  imports: [CommonModule], // ✅ Ajout de CommonModule pour *ngFor
  template: `
    <h2>📊 Attribuer les cotes aux joueurs</h2>
    <div *ngFor="let joueur of data.joueurs">
      <p>{{ joueur.nom }} - Cote : {{ joueur.moyenneCote }}</p>
      <button (click)="modifierCote(joueur, -0.5)">➖ 0.5</button>
      <button (click)="modifierCote(joueur, 0.5)">➕ 0.5</button>
    </div>
    <button (click)="validerCotes()">✅ Valider</button>
  `,
  styles: [`
    h2 { text-align: center; }
    div { margin-bottom: 10px; }
    button { margin: 5px; }
  `]
})
export class CoteAttributionDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<CoteAttributionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { joueurs: FeuilleDeMatchDTO[] }
  ) {}

  modifierCote(joueur: FeuilleDeMatchDTO, valeur: number): void {
    joueur.moyenneCote = (joueur.moyenneCote || 5) + valeur;
  }

  validerCotes(): void {
    const cotes: Record<number, number> = this.data.joueurs.reduce((acc: Record<number, number>, joueur) => {
      acc[joueur.jid] = joueur.moyenneCote;
      return acc;
    }, {} as Record<number, number>); // ✅ Correction du typage avec `Record<number, number>`
  
    this.dialogRef.close(cotes);
  }
}

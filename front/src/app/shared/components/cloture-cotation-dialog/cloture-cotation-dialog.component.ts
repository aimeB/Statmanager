import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CommonModule } from '@angular/common'; // ✅ Import nécessaire


@Component({
  selector: 'app-cloture-cotation-dialog',
  standalone: true, // ✅ Important si tu utilises des composants standalone
  imports: [CommonModule], // ✅ Import de CommonModule
  templateUrl: './cloture-cotation-dialog.component.html',
  styleUrls: ['./cloture-cotation-dialog.component.scss']
})
export class ClotureCotationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ClotureCotationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { joueurs: any[] }
  ) {}

  // ✅ Augmenter la note d'un joueur
  incrementCote(joueur: any): void {
    if (joueur.cote < 10) {
      joueur.cote += 0.5;
    }
  }

  // ✅ Diminuer la note d'un joueur
  decrementCote(joueur: any): void {
    if (joueur.cote > 0) {
      joueur.cote -= 0.5;
    }
  }

  // ✅ Fermer la popup en validant
  valider(): void {
    this.dialogRef.close(this.data.joueurs);
  }

  // ✅ Annuler la clôture
  annuler(): void {
    this.dialogRef.close(null);
  }
}

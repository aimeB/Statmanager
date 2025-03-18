import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FeuilleDeMatchDTO } from '../../../../app/modules/feuille-de-match/models/feuille-de-match.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cote-attribution-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="popup-content">
      <!-- ✅ Titre avec icône -->
      <div class="popup-header">
        <img src="assets/cote-icon.png" alt="Cotes" class="popup-logo" />
        <h2>Évaluation des Joueurs</h2>
      </div>

      <!-- ✅ Conteneur scrollable pour éviter le débordement -->
      <div class="scroll-container">
        <div *ngFor="let joueur of data.joueurs" class="joueur-cote">
          <p class="joueur-nom">{{ joueur.nom }}</p>
          <div class="cote-actions">
            <button (click)="modifierCote(joueur, -0.5)">➖</button>
            <span class="joueur-cote-valeur">{{ joueur.cote }}</span>
            <button (click)="modifierCote(joueur, 0.5)">➕</button>
          </div>
        </div>
      </div>

      <!-- ✅ Bouton de validation -->
      <button class="btn-valider" (click)="validerCotes()">✅ Valider</button>

      <!-- ✅ Bouton de fermeture -->
      <button class="popup-close" (click)="dialogRef.close()">✖</button>
    </div>
  `,
  styles: [
    `
    /* ✅ Arrière-plan assombri avec flou */
    .popup-container {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background-color: rgba(0, 0, 0, 0.6);
      backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    /* ✅ Pop-up */
    .popup-content {
      background-color: #222;
      color: #ffd700;
      padding: 3vh 3vw;
      border-radius: 1.5vw;
      max-width: 600px;
      width: 90%;
      text-align: center;
      box-shadow: 0px 6px 15px rgba(0, 0, 0, 0.5);
      max-height: 75vh;
      overflow-y: auto;
      position: relative;
    }

    /* ✅ Titre */
    .popup-header {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 1vw;
      margin-bottom: 2vh;
    }

    .popup-logo {
      width: 3vw;
      height: auto;
    }

    .popup-header h2 {
      font-size: 2vw;
      color: #ffd700;
      text-transform: uppercase;
    }

    /* ✅ Conteneur des joueurs */
    .scroll-container {
      max-height: 55vh;
      overflow-y: auto;
      padding-right: 1vw;
    }

    /* ✅ Style des joueurs */
    .joueur-cote {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: rgba(255, 255, 255, 0.1);
      padding: 1vh 1.5vw;
      border-radius: 0.5vw;
      margin: 0.5vh 0;
      font-size: 1.2vw;
    }

    .joueur-nom {
      flex: 1;
      text-align: left;
      font-weight: bold;
      color: white;
    }

    /* ✅ Actions de notation */
    .cote-actions {
      display: flex;
      align-items: center;
      gap: 1vw;
    }

    .joueur-cote-valeur {
      font-size: 1.5vw;
      font-weight: bold;
      color: #ffd700;
    }

    /* ✅ Boutons */
    .joueur-cote button {
      background: #ffd700;
      border: none;
      padding: 0.5vh 1vw;
      border-radius: 0.5vw;
      cursor: pointer;
      font-size: 1vw;
      font-weight: bold;
      transition: all 0.2s;
    }

    .joueur-cote button:hover {
      background: #e6c200;
    }

    /* ✅ Bouton de validation */
    .btn-valider {
      width: 100%;
      background: #28a745;
      color: white;
      font-size: 1.3vw;
      font-weight: bold;
      border: none;
      padding: 1vh;
      border-radius: 0.5vw;
      cursor: pointer;
      margin-top: 2vh;
      transition: background 0.2s;
    }

    .btn-valider:hover {
      background: #218838;
    }

    /* ✅ Bouton de fermeture */
    .popup-close {
      position: absolute;
      top: 1vh;
      right: 1vw;
      background: transparent;
      color: white;
      font-size: 2vw;
      border: none;
      cursor: pointer;
      transition: color 0.2s;
    }

    .popup-close:hover {
      color: #ffd700;
    }
    `,
  ],
})
export class CoteAttributionDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<CoteAttributionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { joueurs: FeuilleDeMatchDTO[] },
  ) {
    console.log('Joueurs reçus dans la pop-up:', this.data.joueurs);
  }

  modifierCote(joueur: FeuilleDeMatchDTO, valeur: number): void {
    joueur.cote = (joueur.cote || 5) + valeur;
  }

  validerCotes(): void {
    const cotes: Record<number, number> = this.data.joueurs.reduce(
      (acc: Record<number, number>, joueur) => {
        acc[joueur.jid] = joueur.cote;
        return acc;
      },
      {} as Record<number, number>,
    );

    this.dialogRef.close(cotes);
  }
}

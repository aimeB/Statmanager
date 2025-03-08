import { Component, OnInit, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { TerrainDTO } from '../../../rencontre/models/terrain.model';

import { FeuilleDeMatchDTO } from '../../../feuille-de-match/models/feuille-de-match.model';
import { ActivatedRoute } from '@angular/router';
import { TerrainService } from '../../services/terrain.service';
import { MatDialog } from '@angular/material/dialog';
import { JoueurSelectionDialogComponent } from '../../components/../../joueur/components/joueur-selection-dialog/joueur-selection-dialog.component';
import { MatDialogModule } from '@angular/material/dialog';
import { ChangeDetectorRef } from '@angular/core';



@Component({
  selector: 'app-terrain',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    FormsModule,
    MatChipsModule,
    MatIconModule,
    RouterModule,
    MatCardModule,
    MatDialogModule,

  ],
  templateUrl: './terrain.component.html',
  styleUrls: ['./terrain.component.scss']
})
export class TerrainComponent implements OnInit {
  terrain: TerrainDTO | null = null;
  joueursDisponibles: FeuilleDeMatchDTO[] = [];
  joueurSelectionne: FeuilleDeMatchDTO | null = null;
  posteSelectionne: string | null = null;

  postesFixes: { key: string; label: string; joueur: FeuilleDeMatchDTO | null }[] = [
    { key: 'GB', label: '🥅 Gardien', joueur: null },
    { key: 'DD', label: '🛡️ Défenseur Droit', joueur: null },
    { key: 'DC_GAUCHE', label: '🛡️ DC Gauche', joueur: null },
    { key: 'DC_DROIT', label: '🛡️ DC Droit', joueur: null },
    { key: 'DG', label: '🛡️ Défenseur Gauche', joueur: null },
    { key: 'MO', label: '🎯 Milieu Offensif', joueur: null },
    { key: 'MDF', label: '🎯 Milieu Défensif', joueur: null },
    { key: 'MR', label: '🎯 Milieu Relayeur', joueur: null },
    { key: 'AID', label: '⚡ Ailier Droit', joueur: null },
    { key: 'AC', label: '🔥 Buteur', joueur: null },
    { key: 'AIG', label: '⚡ Ailier Gauche', joueur: null }
  ];

  constructor(
    private terrainService: TerrainService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {}



  ngOnInit(): void {
    const idRencontre = Number(this.route.snapshot.paramMap.get('idRencontre'));
    const state = history.state;
    const terrainDTO = state?.terrain;
    console.log("✅ Terrain récupéré via `history.state`");
  
    console.log("📌 Données récupérées via `history.state` :", terrainDTO);
  
    if (terrainDTO && Object.keys(terrainDTO).length > 0) {
      console.log("✅ Terrain récupéré via `history.state`");
      this.terrain = terrainDTO;
      this.initialiserTerrain();
    } else {
      console.warn("⚠️ `history.state` vide, récupération du terrain depuis l'API...");
      this.chargerTerrainDepuisAPI(idRencontre);
    }
  }
  
  

  initialiserTerrain(): void {
    this.joueursDisponibles = [...this.terrain!.titulaires];

    for (let joueur of this.joueursDisponibles) {
      const posteExact = this.postesFixes.find(p => p.key === joueur.poste && !p.joueur);
      if (posteExact) {
        posteExact.joueur = joueur;
        continue;
      }
    }
  }


  chargerTerrainDepuisAPI(idRencontre: number): void {
    if (!idRencontre) {
      console.error("❌ ID de rencontre manquant, impossible de récupérer le terrain.");
      return;
    }
  
    this.terrainService.getTerrain(idRencontre).subscribe({
      next: (terrain) => {
        console.log("✅ Terrain récupéré via API :", terrain);
        this.terrain = terrain;
        this.initialiserTerrain();
      },
      error: (err) => {
        console.error("❌ Erreur lors de la récupération du terrain depuis l'API :", err);
        alert("❌ Impossible de récupérer le terrain !");
      }
    });
  }
  





  echangerJoueurs(joueur: FeuilleDeMatchDTO): void {
    if (!this.posteSelectionne || !this.terrain) return;

    const joueurActuel = this.terrain.terrainJoueurs[this.posteSelectionne];

    if (joueurActuel && joueurActuel.id !== -1) {
      const posteDuJoueurSelectionne = Object.keys(this.terrain.terrainJoueurs).find(
        key => this.terrain!.terrainJoueurs[key]?.id === joueur.id
      );

      if (posteDuJoueurSelectionne) {
        const joueurTemporaire = this.terrain.terrainJoueurs[posteDuJoueurSelectionne];

        this.terrain.terrainJoueurs[posteDuJoueurSelectionne] = joueurActuel;
        this.terrain.terrainJoueurs[this.posteSelectionne] = joueur;
      }
    } else {
      this.terrain.terrainJoueurs[this.posteSelectionne] = joueur;
      this.joueursDisponibles = this.joueursDisponibles.filter(j => j.id !== joueur.id);
    }

    this.posteSelectionne = null;
    console.log('✅ Échange effectué sans rafraîchir le terrain.');
  }

  validerPlacement(): void {
    if (!this.terrain) {
      console.error('❌ Terrain non initialisé.');
      return;
    }

    console.log('✅ Terrain finalisé :', this.terrain);

    this.terrainService.construireTerrain(this.terrain!.idRencontre, this.terrain!).subscribe({
      next: (terrainConfirme) => {
        console.log('✅ Terrain confirmé dans construireTerrain -- validerPlacement  :', terrainConfirme);
        alert('🎯 Partie lancée avec succès !');

        this.router.navigate(['/detail-terrain', this.terrain!.idRencontre], {
          state: { terrain: terrainConfirme }
        });
      },
      error: (err) => {
        console.error('❌ Erreur lors de la validation du terrain :', err);
        alert('❌ Une erreur est survenue lors de l\'envoi des données !');
      }
    });
  }


/**
 * 📌 TrackBy pour améliorer les performances avec *ngFor
 */
trackByPoste(index: number, poste: { key: string; label: string; joueur: FeuilleDeMatchDTO | null }): string {
  return poste.key;
}

/**
 * 📌 Ouvre la sélection d’un joueur pour un poste donné
 */
ouvrirSelectionJoueur(posteKey: string): void {
  this.posteSelectionne = posteKey;
  const joueurActuel = this.terrain?.terrainJoueurs[posteKey];

  const joueursProposables: FeuilleDeMatchDTO[] = this.joueursDisponibles.filter(joueur => joueur.poste === posteKey);

  const dialogRef = this.dialog.open(JoueurSelectionDialogComponent, {
    data: { joueurs: joueursProposables.length > 0 ? joueursProposables : [{ id: -1, nom: "Aucun joueur disponible", poste: "" }] }
  });

  dialogRef.afterClosed().subscribe((joueurSelectionne: FeuilleDeMatchDTO) => {
    if (joueurSelectionne) {
      this.terrain!.terrainJoueurs[posteKey] = joueurSelectionne;
      this.joueursDisponibles = this.joueursDisponibles.filter(j => j.id !== joueurSelectionne.id);
    }
  });
}


/**
 * 📌 Réinitialiser un poste (enlever le joueur)
 */
reinitialiserPoste(poste: any): void {
  if (!poste.joueur) return;
  
  // ✅ Ajouter le joueur retiré aux joueurs disponibles
  this.joueursDisponibles.push(poste.joueur);
  poste.joueur = null; // ✅ Supprimer le joueur du poste
  console.log(`♻️ Poste ${poste.key} réinitialisé`);
}

/**
 * 📌 Optimisation des performances avec trackBy pour *ngFor
 */
trackByJoueur(index: number, joueur: FeuilleDeMatchDTO): number {
  return joueur.id;
}

/**
 * 📌 Vérifier si tous les postes sont remplis avant de valider
 */
estTerrainComplet(): boolean {
  return !!this.terrain && Object.values(this.terrain.terrainJoueurs).every(j => j !== null);
}




/**
 * 📌 Sélectionner un joueur dans la liste des disponibles
 */
selectionnerJoueur(joueur: FeuilleDeMatchDTO): void {
  this.joueurSelectionne = joueur;
  console.log(`📌 Joueur sélectionné : ${joueur.nom}`);
}



}

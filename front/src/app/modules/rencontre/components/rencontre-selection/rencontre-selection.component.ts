import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { JoueurDTO } from '../../../joueur/models/joueur.model';
import { RencontreService } from '../../services/rencontre.service';
import { VestiaireDTO } from '../../../rencontre/models/rencontre.model';
import { Division } from '../../../../shared/models/division.enum';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { TerrainDTO } from '../../../rencontre/models/terrain.model';

@Component({
  selector: 'app-rencontre-selection',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatSelectModule,
    MatInputModule,
    MatCheckboxModule,
    RouterModule,
  ],
  templateUrl: './rencontre-selection.component.html',
  styleUrls: ['./rencontre-selection.component.scss'],
})
export class RencontreSelectionComponent implements OnInit {
  @Input() idChampionnat!: number;
  nomAdversaire: string = '';
  divisionAdversaire: string = '';
  titulairesSelectionnes: JoueurDTO[] = [];
  joueursDisponibles: JoueurDTO[] = [];

  constructor(
    private rencontreService: RencontreService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('idChampionnat');
    if (!id) {
      console.error('❌ ID du championnat manquant !');
      return;
    }
    this.idChampionnat = +id;
    this.recupererSelectionRencontre();
  }

  /**
   * ✅ Récupère la sélection des joueurs depuis le backend
   */
  recupererSelectionRencontre(): void {
    this.rencontreService.getNouvelleRencontreSelection(this.idChampionnat).subscribe({
      next: (data) => {
        console.log("✅ Données récupérées :", data);

        const tousLesJoueurs = data.joueursDisponibles;
        const titulairesDernierMatch = data.joueursPrecedents.slice(0, 11);

        // ✅ Ajout automatique des titulaires du dernier match
        this.titulairesSelectionnes = [...titulairesDernierMatch];

        // ✅ Retirer ces titulaires des joueurs disponibles
        this.joueursDisponibles = tousLesJoueurs.filter(
          (joueur) => !titulairesDernierMatch.some((titulaire) => titulaire.id === joueur.id)
        );

        this.nomAdversaire = data.nomAdversaire || "";
        this.divisionAdversaire = data.divisionAdversaire || "";

        console.log("📌 Titulaires sélectionnés automatiquement :", this.titulairesSelectionnes);
        console.log("📌 Joueurs disponibles mis à jour :", this.joueursDisponibles);
      },
      error: (err) => {
        console.error("❌ Erreur lors de la récupération des joueurs :", err);
      },
    });
  }

  /**
   * 📌 Gère la sélection et désélection des titulaires
   */
  selectionnerTitulaires(joueur: JoueurDTO, origine: 'disponibles' | 'titulaires'): void {
    if (origine === 'titulaires') {
      this.titulairesSelectionnes = this.titulairesSelectionnes.filter(j => j.id !== joueur.id);
      this.joueursDisponibles.push(joueur);
      console.log(`🔄 Joueur retiré des titulaires et remis en disponible: ${joueur.nom}`);
      return;
    }

    if (this.titulairesSelectionnes.length >= 11) {
      alert('❌ Vous avez déjà sélectionné 11 titulaires !');
      return;
    }

    console.log(`🔹 Sélection du joueur : ${joueur.nom}`);

    // ✅ Retirer le joueur de la liste disponible
    this.joueursDisponibles = this.joueursDisponibles.filter(j => j.id !== joueur.id);

    // ✅ Ajouter aux titulaires sélectionnés
    this.titulairesSelectionnes.push(joueur);
    console.log(`✅ Ajouté à titulairesSelectionnes : ${joueur.nom}`);

    // 📌 Vérification des listes après mise à jour
    console.log("📌 Joueurs Disponibles:", this.joueursDisponibles.map(j => j.nom));
    console.log("📌 Titulaires Sélectionnés:", this.titulairesSelectionnes.map(j => j.nom));
  }

  /**
   * 📌 Vérifie si la sélection est complète
   */
  estSelectionComplete(): boolean {
    return this.titulairesSelectionnes.length === 11;
  }

  /**
   * 📌 Valide la sélection et envoie les données au backend
   */
  validerSelection(): void {
    if (this.titulairesSelectionnes.length !== 11) {
      alert('❌ Il faut exactement 11 titulaires !');
      return;
    }

    const vestiaireDTO: VestiaireDTO = {
      idChampionnat: this.idChampionnat,
      nomAdversaire: this.nomAdversaire,
      divisionAdversaire: this.divisionAdversaire as Division,
      titulaires: this.titulairesSelectionnes,
      remplacants: [...this.joueursDisponibles],
    };

    console.log('📌 Envoi de la sélection des joueurs au backend :', vestiaireDTO);

    this.rencontreService.validerSelectionVestiaire(vestiaireDTO).subscribe({
      next: (terrain: TerrainDTO) => {
        console.log('✅ Rencontre validée, terrain reçu :', terrain);

        const idRencontre = terrain.idRencontre;
        if (!idRencontre) {
          console.error('❌ ID de rencontre manquant dans la réponse !');
          alert('Erreur : ID de la rencontre manquant !');
          return;
        }

        this.router.navigate(
          [`/championnats/${this.idChampionnat}/rencontres/${idRencontre}/terrain`],
          {
            state: { idRencontre, terrain },
          }
        );

        console.log('📌 Navigation vers terrain avec ID rencontre :', idRencontre, 'et terrain :', terrain);
      },
      error: (err) => {
        console.error('❌ Erreur lors de la validation de la sélection :', err);
        alert('Erreur lors de la validation de la sélection !');
      },
    });
  }

  /**
   * 📌 Convertir le poste d’un joueur pour l'affichage
   */
  convertirPoste(poste: string): string {
    const posteMap: { [key: string]: string } = {
      GB: 'Gardien',
      DD: 'Défenseur Droit',
      DG: 'Défenseur Gauche',
      DC: 'Défenseur Central',
      MDF: 'Milieu Défensif',
      MR: 'Milieu Relayeur',
      MO: 'Milieu Offensif',
      AID: 'Ailier Droit',
      AIG: 'Ailier Gauche',
      SA: 'Soutien Attaque',
      AC: 'Attaquant Central',
    };
    return posteMap[poste] || poste;
  }

  /**
   * 📌 Optimisation de *ngFor avec trackBy
   */
  trackByJoueur(index: number, joueur: JoueurDTO): number {
    return joueur.id;
  }

  trackByDivision(index: number, division: string): string {
    return division;
  }
}

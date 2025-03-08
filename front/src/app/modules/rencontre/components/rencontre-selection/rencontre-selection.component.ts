import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { FeuilleDeMatchDTO } from './../../../feuille-de-match/models/feuille-de-match.model';
import { JoueurDTO } from './../../../joueur/models/joueur.model';
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
    RouterModule
  ],
  templateUrl: './rencontre-selection.component.html',
  styleUrls: ['./rencontre-selection.component.scss']
})
export class RencontreSelectionComponent implements OnInit {
  

  @Input() idChampionnat!: number;  // ✅ Ajout du Input pour lier idChampionnat
  nomAdversaire: string = "";
  divisionAdverse: string = "";
  titulairesSelectionnes: JoueurDTO[] = [];
  joueursDisponibles: JoueurDTO[] = [];
  joueursPrecedents: JoueurDTO[] = [];

  constructor(
    private rencontreService: RencontreService, 
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // ✅ Récupération de l'ID du championnat depuis l'URL
    const id = this.route.snapshot.paramMap.get('idChampionnat');
    if (!id) {
      console.error("❌ ID du championnat manquant !");
      return;
    }
    console.log("✅ ID du Championnat reçu :", this.idChampionnat);

    this.idChampionnat = +id;
    this.recupererSelectionRencontre();
  }

  /**
   * ✅ Récupère la sélection des joueurs depuis le backend
   */
  recupererSelectionRencontre(): void {
    this.rencontreService.getNouvelleRencontreSelection(this.idChampionnat).subscribe({
      next: (data) => {
        this.joueursDisponibles = data.joueursDisponibles;
        this.joueursPrecedents = data.joueursPrecedents;
        this.nomAdversaire = data.nomAdversaire || "";
        this.divisionAdverse = data.divisionAdversaire || "";
        console.log("✅ Sélection des joueurs récupérée :", data);
      },
      error: (err) => {
        console.error("❌ Erreur lors de la récupération des joueurs :", err);
      }
    });
  }

  /**
   * 📌 Gère la sélection et désélection des titulaires
   */
  selectionnerTitulaires(joueur: JoueurDTO, origine: 'disponibles' | 'titulaires'): void {
    if (origine === 'disponibles') {
      if (this.titulairesSelectionnes.length >= 11) {
        alert("❌ Vous avez déjà sélectionné 11 titulaires !");
        return;
      }
      this.joueursDisponibles = this.joueursDisponibles.filter(j => j.id !== joueur.id);
      this.titulairesSelectionnes.push(joueur);
    } else {
      this.titulairesSelectionnes = this.titulairesSelectionnes.filter(j => j.id !== joueur.id);
      if (this.joueursPrecedents.some(j => j.id === joueur.id)) {
        this.joueursPrecedents.push(joueur);
      } else {
        this.joueursDisponibles.push(joueur);
      }
    }
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
    alert("❌ Il faut exactement 11 titulaires !");
    return;
  }

  const vestiaireDTO: VestiaireDTO = {
    idChampionnat: this.idChampionnat,
    nomAdversaire: this.nomAdversaire,
    divisionAdversaire: this.divisionAdverse as Division,
    titulaires: this.titulairesSelectionnes,
    remplacants: [...this.joueursDisponibles, ...this.joueursPrecedents]
  };

  console.log("📌 Envoi de la sélection des joueurs au backend :", vestiaireDTO);

  // ✅ Étape 1 : Validation et création de la rencontre
  this.rencontreService.validerSelectionVestiaire(vestiaireDTO).subscribe({
    next: (terrain: TerrainDTO) => { 
      console.log("✅ Rencontre validée, terrain reçu :", terrain);

      // 🔹 Vérification correcte de l'ID de la rencontre
      const idRencontre = terrain.idRencontre

      if (!idRencontre) {
        console.error("❌ ID de rencontre manquant dans la réponse !");
        alert("Erreur : ID de la rencontre manquant !");
        return;
      }

      console.log("✅ ID de la rencontre détecté :", idRencontre);

      // ✅ Étape 2 : Navigation vers le terrain
      this.router.navigate([`/championnats/${this.idChampionnat}/rencontres/${idRencontre}/terrain`], {
        state: { idRencontre, terrain }
      });

      console.log("📌 Navigation vers terrain avec ID rencontre :", idRencontre, "et terrain :", terrain);
    },
    error: (err) => {
      console.error("❌ Erreur lors de la validation de la sélection :", err);
      alert("Erreur lors de la validation de la sélection !");
    }
  });
}


  
  /**
   * 📌 Optimisation de *ngFor avec trackBy
   */
  trackByJoueur(index: number, joueur: JoueurDTO): number {
    return joueur.id;
  }

  /**
   * 📌 Convertir le poste d’un joueur pour l'affichage
   */
  convertirPoste(poste: string): string {
    const posteMap: { [key: string]: string } = {
      "GB": "Gardien",
      "DD": "Défenseur Droit",
      "DG": "Défenseur Gauche",
      "DC": "Défenseur Central",
      "MDF": "Milieu Défensif",
      "MR": "Milieu Relayeur",
      "MO": "Milieu Offensif",
      "AID": "Ailier Droit",
      "AIG": "Ailier Gauche",
      "AC": "Attaquant Central"
    };
    return posteMap[poste] || poste;
  }



  trackByDivision(index: number, division: string): string {
    return division;
  }
  
}




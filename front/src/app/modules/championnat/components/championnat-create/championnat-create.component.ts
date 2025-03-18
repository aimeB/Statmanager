import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { ChampionnatCreationDTO } from '../../models/championnat.model';
import { ChampionnatService } from '../../services/championnat.service';
import { JoueurService } from '../../../joueur/services/joueur.service';
import { Division } from '../../../../shared/models/division.enum';
import { JoueurLightDTO } from '../../../joueur/models/joueur.model';
import { Router } from '@angular/router'; // ✅ Import du Router
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-championnat-create',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    RouterModule,
  ],
  templateUrl: './championnat-create.component.html',
  styleUrls: ['./championnat-create.component.scss'],
})
export class ChampionnatCreateComponent {
  divisions: Division[] = ['DIV1', 'DIV2', 'DIV3', 'DIV4'];
  joueursDisponibles: JoueurLightDTO[] = [];
  joueursSelectionnes: number[] = [];
  divisionSelectionnee: Division = 'DIV1';

  constructor(
    private championnatService: ChampionnatService,
    private joueurService: JoueurService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.chargerJoueurs();
  }

  chargerJoueurs(): void {
    this.joueurService.getJoueursLight().subscribe({
      next: (joueurs) => {
        this.joueursDisponibles = joueurs;
      },
      error: (err) => {
        console.log('✅ Championnat créé avec succès !', this.joueursDisponibles);
        console.error('❌ Erreur lors du chargement des joueurs', err);
        alert('Impossible de charger la liste des joueurs.');
      },
    });
  }

  selectionnerJoueur(joueur: JoueurLightDTO): void {
    if (this.joueursSelectionnes.includes(joueur.id)) {
      this.joueursSelectionnes = this.joueursSelectionnes.filter((id) => id !== joueur.id);
    } else {
      if (this.joueursSelectionnes.length >= 23) {
        alert('Vous ne pouvez sélectionner que 23 joueurs !');
        return;
      }
      this.joueursSelectionnes.push(joueur.id);
    }
  }
  creerChampionnat(): void {
    if (this.joueursSelectionnes.length !== 23) {
      alert('Veuillez sélectionner exactement 23 joueurs !');
      return;
    }

    const dto: ChampionnatCreationDTO = {
      division: this.divisionSelectionnee,
      joueursIds: this.joueursSelectionnes,
    };

    this.championnatService.creerChampionnat(dto).subscribe({
      next: (championnat) => {
        console.log('✅ Championnat créé avec succès !', championnat);
        alert(`Championnat ${championnat.division} créé avec succès !`);

        // ✅ Préparer la liste des joueurs sélectionnés
        const joueursSelectionnesDetails = this.joueursDisponibles.filter((joueur) =>
          this.joueursSelectionnes.includes(joueur.id),
        );

        // ✅ Redirection avec les joueurs sélectionnés
        this.router.navigate(['/rencontres/selection'], {
          state: {
            championnatId: championnat.idChamp, // ✅ ID du championnat
            division: championnat.division, // ✅ Division sélectionnée
            joueursSelectionnes: joueursSelectionnesDetails, // ✅ Liste des joueurs sélectionnés
          },
        });
      },
      error: (err) => {
        console.error('❌ Erreur lors de la création du championnat', err);
        alert('Erreur lors de la création du championnat');
      },
    });
  }
}

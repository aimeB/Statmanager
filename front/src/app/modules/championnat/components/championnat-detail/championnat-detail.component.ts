import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChampionnatService } from '../../services/championnat.service';
import { ChampionnatDetailWithRencontresDTO } from '../../models/championnat.model';
import { StatistiquesChampionnatDTO } from '../../../statistiques/models/statistiques.model';
import { StatistiquesRencontreDTO } from '../../../statistiques/models/statistiques.model';
import { NgIf, NgFor } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-championnat-detail',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor],
  templateUrl: './championnat-detail.component.html',
  styleUrls: ['./championnat-detail.component.scss'],
})
export class ChampionnatDetailComponent implements OnChanges {
  @Input() idChampionnat!: number;
  championnatDetail?: ChampionnatDetailWithRencontresDTO;
  statistiquesChampionnat: StatistiquesChampionnatDTO[] = [];
  selectedRencontreId?: number;
  statistiquesRencontre: StatistiquesRencontreDTO[] = [];

  constructor(
    private championnatService: ChampionnatService,
    private router: Router,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['idChampionnat'] && this.idChampionnat) {
      this.loadChampionnatDetail();
      this.loadStatistiquesChampionnat();
    }
  }

  /** 📌 Charge les détails d'un championnat */
  loadChampionnatDetail(): void {
    if (!this.idChampionnat) return;

    this.championnatService.getChampionnatWithRencontres(this.idChampionnat).subscribe({
      next: (data) => {
        this.championnatDetail = data;
        console.log('✅ Championnat chargé :', data);
      },
      error: (err) => console.error('❌ Erreur lors du chargement des détails du championnat', err),
    });
  }

  /** 📌 Charge les statistiques des joueurs dans le championnat */
  loadStatistiquesChampionnat(): void {
    if (!this.idChampionnat) return;

    this.championnatService.getStatistiquesChampionnat(this.idChampionnat).subscribe({
      next: (data) => {
        this.statistiquesChampionnat = data;
        console.log('✅ Statistiques du championnat chargées :', data);
      },
      error: (err) =>
        console.error('❌ Erreur lors du chargement des statistiques du championnat', err),
    });
  }

  /** 📌 Charge les statistiques d’une rencontre sélectionnée */
  loadStatistiquesRencontre(idRencontre: number): void {
    this.championnatService.getStatistiquesRencontre(idRencontre).subscribe({
      next: (data) => {
        this.statistiquesRencontre = data;
        console.log(`✅ Statistiques de la rencontre ${idRencontre} chargées :`, data);
      },
      error: (err) =>
        console.error('❌ Erreur lors du chargement des statistiques de la rencontre', err),
    });
  }

  /** 📌 Sélection d’une rencontre et chargement des statistiques associées */
  selectRencontre(idRencontre: number): void {
    if (this.selectedRencontreId === idRencontre) {
      this.selectedRencontreId = undefined;
      this.statistiquesRencontre = [];
    } else {
      this.selectedRencontreId = idRencontre;
      this.loadStatistiquesRencontre(idRencontre);
    }
  }

  creerNouvelleRencontre(): void {
    if (!this.idChampionnat) return;
    this.router.navigate([`/championnats/${this.idChampionnat}/rencontres/selection`]);
  }
}

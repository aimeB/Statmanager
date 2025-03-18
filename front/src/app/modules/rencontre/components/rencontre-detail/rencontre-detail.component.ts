import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChampionnatService } from '../../../championnat/services/championnat.service';
import { StatistiquesRencontreDTO } from '../../../statistiques/models/statistiques.model';
import { NgIf, NgFor } from '@angular/common';

@Component({
  selector: 'app-rencontre-detail',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor],
  templateUrl: './rencontre-detail.component.html',
  styleUrls: ['./rencontre-detail.component.scss'],
})
export class RencontreDetailComponent implements OnChanges {
  @Input() idRencontre!: number;
  statsJoueurs: StatistiquesRencontreDTO[] = [];

  constructor(private championnatService: ChampionnatService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['idRencontre']) {
      this.loadStatistiquesRencontre();
    }
  }

  /** 📌 Charge les statistiques des joueurs d'une rencontre */
  loadStatistiquesRencontre(): void {
    this.championnatService.getStatistiquesRencontre(this.idRencontre).subscribe({
      next: (data) => (this.statsJoueurs = data),
      error: (err) =>
        console.error('❌ Erreur lors du chargement des statistiques de la rencontre', err),
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatistiqueService } from '../../services/statistique.service';
import { StatCompositeDTO } from '../../../joueur/models/joueur.model';

@Component({
  selector: 'app-stat-composite',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stat-composite.component.html',
  styleUrls: ['./stat-composite.component.scss'],
})
export class StatCompositeComponent implements OnInit {
  joueursComposite!: StatCompositeDTO;

  constructor(private statistiqueService: StatistiqueService) {}

  ngOnInit(): void {
    this.chargerJoueursParScoreComposite();
  }

  chargerJoueursParScoreComposite(): void {
    this.statistiqueService.getJoueursParScoreComposite().subscribe({
      next: (data) => (this.joueursComposite = data),
      error: (err) =>
        console.error('❌ Erreur lors du chargement des joueurs par score composite', err),
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatistiqueService } from '../../services/statistique.service';
import { StatTopJoueurDTO } from '../../../joueur/models/joueur.model';

@Component({
  selector: 'app-stat-top-joueur',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stat-top-joueur.component.html',
  styleUrls: ['./stat-top-joueur.component.scss'],
})
export class StatTopJoueurComponent implements OnInit {
  topJoueurs!: StatTopJoueurDTO;

  constructor(private statistiqueService: StatistiqueService) {}

  ngOnInit(): void {
    this.chargerMeilleursJoueurs();
  }

  chargerMeilleursJoueurs(): void {
    this.statistiqueService.getMeilleursJoueurs().subscribe({
      next: (data) => (this.topJoueurs = data),
      error: (err) => console.error('❌ Erreur lors du chargement des meilleurs joueurs', err),
    });
  }
}

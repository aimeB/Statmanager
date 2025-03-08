import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatistiqueService } from '../../services/statistique.service';
import { StatCritereDTO } from '../../../joueur/models/joueur.model';

@Component({
  selector: 'app-stat-critere',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stat-critere.component.html',
  styleUrls: ['./stat-critere.component.scss']
})
export class StatCritereComponent implements OnInit {
  stats!: StatCritereDTO;

  constructor(private statistiqueService: StatistiqueService) {}

  ngOnInit(): void {
    this.chargerStats('buts'); // Par défaut, tri par buts
  }

  chargerStats(critere: string): void {
    this.statistiqueService.getJoueursParCritere(critere).subscribe({
      next: (data) => (this.stats = data),
      error: (err) => console.error('❌ Erreur lors du chargement des statistiques', err)
    });
  }
}

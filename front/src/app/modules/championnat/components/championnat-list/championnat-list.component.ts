import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChampionnatService } from '../../services/championnat.service';
import { ChampionnatLightDTO } from '../../models/championnat.model';
import { ChampionnatDetailComponent } from '../championnat-detail/championnat-detail.component';
import { NgIf, NgFor } from '@angular/common';

@Component({
  selector: 'app-championnat-list',
  standalone: true,
  imports: [CommonModule, ChampionnatDetailComponent, NgIf, NgFor], // ✅ Import des directives nécessaires
  template: `
    <div class="championnats-container">
      <h2>🏆 Derniers Championnats</h2>
      
      <ul>
        <li *ngFor="let championnat of championnats" (click)="selectChampionnat(championnat.idChamp)">
        Championnat n°{{ championnat.idChamp }}     ------   {{ championnat.division }} - {{ championnat.statut }}
        </li>
      </ul>

      <app-championnat-detail *ngIf="selectedChampionnatId" [idChampionnat]="selectedChampionnatId"></app-championnat-detail>
    </div>
  `,
  styleUrls: ['./championnat-list.component.scss']
})
export class ChampionnatListComponent implements OnInit {
  championnats: ChampionnatLightDTO[] = [];
  selectedChampionnatId?: number;

  constructor(private championnatService: ChampionnatService) {}

  ngOnInit(): void {
    this.loadChampionnats();
  }

  /** 📌 Charge la liste des 10 derniers championnats */
  loadChampionnats(): void {
    this.championnatService.getChampionnatOverview().subscribe({
      next: (data) => this.championnats = data.derniersChampionnats,
      error: (err) => console.error('❌ Erreur lors du chargement des championnats', err)
    });
  }

  /** 📌 Sélectionne un championnat et affiche ses détails */
  selectChampionnat(idChampionnat: number): void {
    this.selectedChampionnatId = idChampionnat;
  }
}

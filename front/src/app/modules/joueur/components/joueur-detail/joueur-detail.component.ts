import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { JoueurService } from '../../services/joueur.service';
import { JoueurStatGlobalDTO } from '../../models/joueur.model';

@Component({
  selector: 'app-joueur-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './joueur-detail.component.html',
  styleUrls: ['./joueur-detail.component.scss'],
})
export class JoueurDetailComponent implements OnInit {
  joueurData!: JoueurStatGlobalDTO;
  idJoueur?: number;
  nomJoueur?: string;

  constructor(
    private route: ActivatedRoute,
    private joueurService: JoueurService,
  ) {}

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id'); // Peut être un ID ou un nom

    if (!isNaN(Number(param))) {
      // ✅ Si c'est un ID, on le convertit en nombre et on charge les stats
      this.idJoueur = Number(param);
      this.chargerJoueurDetailsParId(this.idJoueur);
    } else {
      // ✅ Sinon, c'est un nom et on cherche le joueur par son nom
      this.nomJoueur = param!;
      this.chargerJoueurDetailsParNom(this.nomJoueur);
    }
  }

  chargerJoueurDetailsParId(id: number): void {
    this.joueurService.getJoueurStats(id).subscribe({
      next: (data) => (this.joueurData = data),
      error: (err) =>
        console.error('❌ Erreur lors du chargement des données du joueur par ID', err),
    });
  }

  chargerJoueurDetailsParNom(nom: string): void {
    this.joueurService.getJoueurStatsByNom(nom).subscribe({
      next: (data) => (this.joueurData = data),
      error: (err) =>
        console.error('❌ Erreur lors du chargement des données du joueur par Nom', err),
    });
  }
}

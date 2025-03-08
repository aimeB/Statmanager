import { Component, OnInit } from '@angular/core';
import { JoueurService } from '../../services/joueur.service';
import { StatistiquesDTO } from './../../../statistiques/models/statistiques.model';
import { CommonModule } from '@angular/common'; // ✅ Import pour les pipes Angular
import { HttpClientModule } from '@angular/common/http';
import { MatTableModule } from '@angular/material/table';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-joueur-list',
  standalone: true,  // ✅ Indique que ce composant est autonome
  imports: [CommonModule, HttpClientModule, MatTableModule, RouterModule], // ✅ Active les pipes comme "number"
  templateUrl: './joueur-list.component.html',
  styleUrls: ['./joueur-list.component.scss']
})
export class JoueurListComponent implements OnInit {
  joueurs: StatistiquesDTO[] = [];
  displayedColumns: string[] = [
    'nom', 'typeJoueur', 'poste', 'buts', 'butsParMinute', 'passes',
    'passesParMinute', 'moyenneButsPasses', 'coteMoyenne', 'coteParMinute',
    'points', 'pointsParMinute', 'totalMinutesJouees'

  ];

  constructor( private joueurService: JoueurService, private router: Router) {}

  ngOnInit(): void {
    this.joueurService.getAllJoueursStats().subscribe((data) => {
      console.log('📊 Liste des joueurs récupérée :', data); // ✅ Debug ici
      this.joueurs = data;
    });
  }



  rechercherJoueurParNom(nom: string) {
    console.warn(`⚠️ Joueur sans ID, recherche par nom: ${nom}`);
    this.joueurService.getJoueurStatsByNom(nom).subscribe((data) => {
      console.log('🔍 Résultat de la recherche par nom :', data);
      if (data && data.joueurId) {
        this.router.navigate(['/joueurs', data.joueurId]); // Redirige vers le bon ID
      } else {
        console.error('❌ Joueur introuvable !');
      }
    });
}
}
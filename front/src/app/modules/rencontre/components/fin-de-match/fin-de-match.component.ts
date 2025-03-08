import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FinMatchDTO } from '../../../rencontre/models/rencontre.model';
import { RencontreService } from '../../services/rencontre.service';


@Component({
  selector: 'app-fin-de-match',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fin-de-match.component.html',
  styleUrl: './fin-de-match.component.scss'
})
export class FinMatchComponent implements OnInit {
  @Input() idRencontre!: number;
  finMatchData!: FinMatchDTO;

  constructor(private route: ActivatedRoute, private rencontreService: RencontreService) {}

  ngOnInit(): void {
    if (!this.idRencontre) {
      this.idRencontre = Number(this.route.snapshot.paramMap.get('idRencontre'));
    }
    this.chargerFinMatch();
  }

  chargerFinMatch(): void {
    this.rencontreService.getFinMatch(this.idRencontre).subscribe({
      next: (data) => (this.finMatchData = data),
      error: (err) => console.error('❌ Erreur lors du chargement des données de fin de match', err)
    });
  }
}

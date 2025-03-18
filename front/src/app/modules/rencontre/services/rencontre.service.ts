import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

// ► Import des DTO utiles
import { ChampionnatDetailWithRencontresDTO } from '../../championnat/models/championnat.model';
import { TerrainDTO } from '../models/terrain.model';
import { FinMatchDTO } from '../models/rencontre.model';
import {
  NouvelleRencontreSelectionDTO,
  VestiaireDTO,
  RencontreDTO,
} from '../models/rencontre.model';
import { JoueurStatGlobalDTO } from '../../joueur/models/joueur.model';

@Injectable({
  providedIn: 'root',
})
export class RencontreService {
  private apiUrl = `${environment.apiBaseUrl}/rencontres`;

  constructor(private http: HttpClient) {}

  /**
   * 📌 Récupère la sélection pour une nouvelle rencontre
   * GET /rencontres/{idChampionnat}/selection
   */
  getNouvelleRencontreSelection(idChampionnat: number): Observable<NouvelleRencontreSelectionDTO> {
    return this.http.get<NouvelleRencontreSelectionDTO>(
      `${this.apiUrl}/${idChampionnat}/selection`,
    );
  }

  /**
   * 📌 Valide la sélection des titulaires et remplaçants du vestiaire
   * POST /rencontres/validerSelection
   */
  validerSelectionVestiaire(vestiaireDTO: VestiaireDTO): Observable<TerrainDTO> {
    return this.http.post<TerrainDTO>(`${this.apiUrl}/validerSelection`, vestiaireDTO);
  }

  /**
   * 📌 Récupère le détail d'un championnat et ses rencontres associées
   * GET /rencontres/{idChamp}/details
   */
  getChampionnatWithRencontres(idChamp: number): Observable<ChampionnatDetailWithRencontresDTO> {
    return this.http.get<ChampionnatDetailWithRencontresDTO>(`${this.apiUrl}/${idChamp}/details`);
  }

  /**
   * 📌 Crée une nouvelle rencontre et retourne l'ID de la rencontre créée
   * POST /rencontres/creer
   */
  creerNouvelleRencontre(vestiaireDTO: VestiaireDTO): Observable<{ rid: number }> {
    return this.http.post<{ rid: number }>(`${this.apiUrl}/creer`, vestiaireDTO);
  }

  /** 📌 Récupère les données de fin de match */
  getFinMatch(idRencontre: number): Observable<FinMatchDTO> {
    return this.http.get<FinMatchDTO>(`${this.apiUrl}/${idRencontre}/fin-match`);
  }
}

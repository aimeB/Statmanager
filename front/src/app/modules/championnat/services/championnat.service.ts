import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from './../../../../environments/environment';
import { StatistiquesChampionnatDTO } from './../../statistiques/models/statistiques.model'; // ✅ Import du modèle
import { StatistiquesRencontreDTO } from './../../statistiques/models/statistiques.model';


// Import des DTO que tu utilises
import { ChampionnatOverviewDTO, ChampionnatDetailWithRencontresDTO, ChampionnatDTO, ChampionnatCreationDTO } from './../models/championnat.model'; // par ex. si tu en as un

// ...

@Injectable({
  providedIn: 'root'
})
export class ChampionnatService {
  // ↓ Par exemple : environment.apiBaseUrl = 'http://localhost:8080/api'
  private apiUrl = `${environment.apiBaseUrl}/championnats`; 
  // ou '/championnats' si c'est ton choix

  constructor(private http: HttpClient) {}

  /**
   * Création d'un championnat (POST /championnat/creer)
   * @param division  division ex: 'DIV4'
   * @param joueursIds  liste d'IDs de joueurs (ex: 23 joueurs)
   */
  creerChampionnat(dto: ChampionnatCreationDTO): Observable<ChampionnatDTO> {
    return this.http.post<ChampionnatDTO>(`${this.apiUrl}/create`, dto);
  }

  /**
   * Récupère un "overview" d'un championnat (ou la liste de 10 derniers si pas d'ID)
   * GET /championnat/overview  ou /championnat/overview/{idChamp}
   */
  getChampionnatOverview(idChampionnat?: number): Observable<ChampionnatOverviewDTO> { 
  const url = idChampionnat ? `${this.apiUrl}/overview/${idChampionnat}` : `${this.apiUrl}/overview`;
  return this.http.get<ChampionnatOverviewDTO>(url); // ✅ S'assurer que c'est un objet et pas un tableau
}


  /**
   * Récupère un championnat avec toutes ses rencontres
   * GET /championnat/{idChamp}/details
   */
  getChampionnatWithRencontres(idChamp: number): Observable<ChampionnatDetailWithRencontresDTO> {
    return this.http.get<ChampionnatDetailWithRencontresDTO>(`${this.apiUrl}/${idChamp}/details`);
  }

/**
 * 📌 Récupère les statistiques des joueurs pour une rencontre donnée.
 * GET /rencontres/{idRencontre}/stats
 */
getStatistiquesRencontre(idRencontre: number): Observable<StatistiquesRencontreDTO[]> {
  return this.http.get<StatistiquesRencontreDTO[]>(`${this.apiUrl}/rencontres/${idRencontre}/stats`);
}


/** 📌 Récupère les statistiques des joueurs sur tout un championnat */
getStatistiquesChampionnat(idChampionnat: number): Observable<StatistiquesChampionnatDTO[]> {
  return this.http.get<StatistiquesChampionnatDTO[]>(`${this.apiUrl}/${idChampionnat}/stats`);
}


}

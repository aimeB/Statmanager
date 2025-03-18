import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from './../../../../environments/environment'; // ✅ Import de l'environnement
// ► Import des interfaces ou classes que tu vas utiliser
import { JoueurDTO } from '../models/joueur.model';
import { JoueurLightDTO, JoueurStatGlobalDTO } from '../models/joueur.model'; // Par ex.
import { StatistiquesDTO } from './../../statistiques/models/statistiques.model';

@Injectable({
  providedIn: 'root',
})
export class JoueurService {
  private apiUrl = `${environment.apiBaseUrl}/joueur`;

  constructor(private http: HttpClient) {}

  /**
   * Récupère la version "light" des joueurs (ID, Nom, Poste).
   * GET /joueur/light
   */
  getJoueursLight(): Observable<JoueurLightDTO[]> {
    return this.http.get<JoueurDTO[]>(`${this.apiUrl}/light`);
  }

  /**
   * Récupère la version "complète" de tous les joueurs.
   * GET /joueur/all
   */
  getJoueurs(): Observable<JoueurDTO[]> {
    return this.http.get<JoueurDTO[]>(`${this.apiUrl}/all`);
  }

  /**
   * 📌 Récupère tous les joueurs avec leurs statistiques globales.
   * GET /api/joueur/stats/all
   */
  getAllJoueursStats(): Observable<StatistiquesDTO[]> {
    return this.http.get<StatistiquesDTO[]>(`${this.apiUrl}/all`);
  }

  /** 📌 Récupère les statistiques globales d'un joueur */
  getJoueurStats(idJoueur: number): Observable<JoueurStatGlobalDTO> {
    return this.http.get<JoueurStatGlobalDTO>(`${this.apiUrl}/${idJoueur}/stats`);
  }

  // 📌 Récupérer un joueur via son Nom
  getJoueurStatsByNom(nom: string): Observable<JoueurStatGlobalDTO> {
    return this.http.get<JoueurStatGlobalDTO>(`${this.apiUrl}/nom/${nom}/stats`);
  }
}

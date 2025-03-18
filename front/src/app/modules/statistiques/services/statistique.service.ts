import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  StatCritereDTO,
  StatTopJoueurDTO,
  StatCompositeDTO,
} from '../models/../../joueur/models/joueur.model';

@Injectable({ providedIn: 'root' })
export class StatistiqueService {
  private apiUrl = `${environment.apiBaseUrl}/joueur`; // ✅ Correction de l'URL

  constructor(private http: HttpClient) {}

  /** 📌 Récupère les joueurs triés selon un critère spécifique */
  getJoueursParCritere(critere: string): Observable<StatCritereDTO> {
    return this.http.get<StatCritereDTO>(`${this.apiUrl}/top/critere?critere=${critere}`);
  }

  /** 📌 Récupère les 5 meilleurs joueurs des dernières rencontres */
  getMeilleursJoueurs(): Observable<StatTopJoueurDTO> {
    return this.http.get<StatTopJoueurDTO>(`${this.apiUrl}/top/meilleurs`);
  }

  /** 📌 Récupère les joueurs triés selon un score composite */
  getJoueursParScoreComposite(): Observable<StatCompositeDTO> {
    return this.http.get<StatCompositeDTO>(`${this.apiUrl}/top/composite`);
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { tap, catchError } from 'rxjs/operators'; // ✅ Ajout de l'import correct
import { TerrainDTO } from '../models/terrain.model'; // ✅ Ajout de l'import correct
import { HistoriqueButDTO } from '../../../shared/models/historique-but.model'; // ✅ Ajout de l'import correct
import { RemplacementDTO, EvenementMatchDTO } from '../models/rencontre.model'; // ✅ Ajout de l'import correct

import { throwError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TerrainService {
  private apiUrl = `${environment.apiBaseUrl}/rencontres`;

  constructor(private http: HttpClient) {}

  getTerrain(idRencontre: number): Observable<TerrainDTO> {
    console.log(`📡 [Service] Envoi requête GET terrain ID=${idRencontre}`);
    return this.http.get<TerrainDTO>(`${this.apiUrl}/${idRencontre}/terrain`);
  }

  /**
   * 📌 Récupère l’historique des buts et remplacements d’une rencontre
   * GET /api/rencontres/{idRencontre}/historique
   */
  getHistoriqueEvenements(idRencontre: number): Observable<EvenementMatchDTO[]> {
    return this.http.get<EvenementMatchDTO[]>(`${this.apiUrl}/${idRencontre}/historique`);
  }

  validerRencontre(dto: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/valider-rencontre`, dto);
  }

  jouerRencontre(idChampionnat: number, idRencontre: number, statsJoueurs: any[]): Observable<any> {
    console.log('📡 Envoi des statistiques au backend...');

    return this.http
      .post(`${this.apiUrl}/${idChampionnat}/${idRencontre}/valider`, statsJoueurs)
      .pipe(
        tap(() => {
          console.log('✅ Rencontre validée, suppression des stats locales.');
          localStorage.removeItem(`stats_${idRencontre}`);
        }),
      );
  }

  mettreAJourAdversaire(
    idRencontre: number,
    nomAdversaire: string,
    scoreAdversaire: number,
    divisionAdverse: string,
  ): Observable<void> {
    console.log(
      "📡 Mise à jour des informations de l'adversaire pour la rencontre ID :",
      idRencontre,
    );

    const adversaireData = { nomAdversaire, scoreAdversaire, divisionAdverse };

    return this.http.put<void>(`${this.apiUrl}/${idRencontre}/adversaire`, adversaireData);
  }

  getListeRemplacement(idRencontre: number, idRemplacant: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${idRencontre}/remplacement/${idRemplacant}`);
  }

  mettreAJourButAdversaire(idRencontre: number, nouveauScore: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${idRencontre}/butAdversaire`, { nouveauScore });
  }

  effectuerRemplacement(
    idRencontre: number,
    remplacementDTO: RemplacementDTO,
  ): Observable<TerrainDTO> {
    return this.http.post<TerrainDTO>(
      `${this.apiUrl}/${idRencontre}/remplacement`,
      remplacementDTO,
    );
  }


  mettreAJourCotes(idRencontre: number, cotes: { [joueurId: number]: number }): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${idRencontre}/maj-cotes`, cotes);
  }
  


  mettreAJourStatsGardien(
    idRencontre: number,
    idJoueur: number,
    butEncaisser: number,
    butArreter: number,
  ): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${idRencontre}/gardien/${idJoueur}`, {

      butEncaisser,
      butArreter,
    });
  }

  // 🔹 Met à jour les arrêts du gardien
  ajouterArretGardien(idRencontre: number, joueurId: number, butArreter: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/gardien/arret/${idRencontre}/${joueurId}`, {
      butArreter,
    });
  }

  updateStatsEnTempsReel(
    idRencontre: number,
    idJoueur: number,
    buts: number,
    idPasseur: number | null,
  ): Observable<TerrainDTO> {
    console.log(
      `📡 [Service] Envoi updateStatsEnTempsReel: idRencontre=${idRencontre}, idJoueur=${idJoueur}, buts=${buts}, idPasseur=${idPasseur}`,
    );

    return this.http
      .post<TerrainDTO>(
        `${this.apiUrl}/${idRencontre}/update-stats?idJoueur=${idJoueur}&buts=${buts}${idPasseur ? `&idPasseur=${idPasseur}` : ''}`,
        {},
      )
      .pipe(
        tap(() => console.log('✅ Requête envoyée avec succès')),
        catchError((err) => {
          console.error("❌ Erreur lors de l'appel à updateStatsEnTempsReel :", err);
          return throwError(() => err);
        }),
      );
  }

  getHistoriqueButs(idRencontre: number): Observable<HistoriqueButDTO[]> {
    return this.http.get<HistoriqueButDTO[]>(`${this.apiUrl}/${idRencontre}/historique-buts`);
  }

  // ✅ Clôturer la rencontre via le backend
  cloturerRencontre(idRencontre: number, clotureDTO: any): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${idRencontre}/cloturer`, clotureDTO);
  }

  // ✅ Récupérer les statistiques en temps réel
  getStatsEnTempsReel(idRencontre: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${idRencontre}/stats`);
  }

  /**
   * 📌 Prépare un terrain modifiable avant validation
   */
  construireTerrain(idRencontre: number): Observable<TerrainDTO> {
    return this.http.post<TerrainDTO>(`${this.apiUrl}/${idRencontre}/construire`, {});
  }

  /**
   * 📌 Valide et initialise un terrain après modification
   */
  initialiserTerrain(idRencontre: number, terrain: TerrainDTO): Observable<TerrainDTO> {
    return this.http.post<TerrainDTO>(`${this.apiUrl}/${idRencontre}/init`, terrain);
  }

  // ✅ Gestion des remplacements
  gererRemplacement(
    idRencontre: number,
    joueurSortantId: number,
    joueurEntrantId: number,
    minute: number,
  ): Observable<void> {
    const remplacementDTO = {
      idRencontre,
      idTitulaireSortant: joueurSortantId,
      remplaçant: { id: joueurEntrantId },
      minuteEntree: minute,
    };

    return this.http.post<void>(`${this.apiUrl}/${idRencontre}/remplacement`, remplacementDTO);
  }
}

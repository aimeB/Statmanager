import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { tap } from 'rxjs/operators'; // ✅ Ajout de l'import correct
import { TerrainDTO } from '../models/terrain.model'; // ✅ Ajout de l'import correct
import { HistoriqueButDTO } from '../../../shared/models/historique-but.model'; // ✅ Ajout de l'import correct
import { RemplacementDTO, EvenementMatchDTO } from '../models/rencontre.model'; // ✅ Ajout de l'import correct


@Injectable({
  providedIn: 'root'
})
export class TerrainService {
  private apiUrl = `${environment.apiBaseUrl}/rencontres`;

  constructor(private http: HttpClient) {}


  construireTerrain(idRencontre: number, terrainDTO: TerrainDTO): Observable<TerrainDTO> {
    console.log("📡 Envoi du TerrainDTO au backend :", terrainDTO);
    return this.http.post<TerrainDTO>(`${this.apiUrl}/${idRencontre}/construire-terrain`, terrainDTO);
  }
  
  getTerrain(idRencontre: number): Observable<TerrainDTO> {
    return this.http.get<TerrainDTO>(`http://localhost:8080/api/rencontres/${idRencontre}/terrain`).pipe(
      tap(terrain => console.log("📥 [Service] Terrain reçu:", terrain))
    );
    
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
    console.log("📡 Envoi des statistiques au backend...");
    
    return this.http.post(`${this.apiUrl}/${idChampionnat}/${idRencontre}/valider`, statsJoueurs).pipe(
      tap(() => {
        console.log("✅ Rencontre validée, suppression des stats locales.");
        localStorage.removeItem(`stats_${idRencontre}`);
      })
    );
  }



  mettreAJourAdversaire(idRencontre: number, nomAdversaire: string, scoreAdversaire: number, divisionAdverse: string): Observable<void> {
    console.log("📡 Mise à jour des informations de l'adversaire pour la rencontre ID :", idRencontre);
  
    const adversaireData = { nomAdversaire, scoreAdversaire, divisionAdverse };
  
    return this.http.put<void>(`${this.apiUrl}/${idRencontre}/adversaire`, adversaireData);
  }
  


  getListeRemplacement(idRencontre: number, idRemplacant: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${idRencontre}/remplacement/${idRemplacant}`);
  }




  
  effectuerRemplacement(idRencontre: number, remplacementDTO: RemplacementDTO): Observable<TerrainDTO> {
    return this.http.post<TerrainDTO>(`${this.apiUrl}/${idRencontre}/remplacement`, remplacementDTO);
  }
  
  

  updateStatsEnTempsReel(idRencontre: number, idFeuilleMatch: number, buts: number, idPasseurFeuille: number | null): Observable<TerrainDTO> {
    const payload = {
      idRencontre,
      idFeuilleMatch,
      buts, // ✅ Ajout de buts dans l'objet envoyé
      idPasseurFeuille
    };
    return this.http.post<TerrainDTO>(`${this.apiUrl}/${idRencontre}/update-stats`, payload);
  }
  

getHistoriqueButs(idRencontre: number): Observable<HistoriqueButDTO[]> {
  return this.http.get<HistoriqueButDTO[]>(`${this.apiUrl}/${idRencontre}/historique-buts`);
}



// ✅ Clôturer la rencontre via le backend
cloturerRencontre(idRencontre: number, clotureDTO: any): Observable<void> {
  return this.http.post<void>(`${this.apiUrl}/${idRencontre}/cloture`, clotureDTO);
}


// ✅ Récupérer les statistiques en temps réel
getStatsEnTempsReel(idRencontre: number): Observable<any> {
  return this.http.get<any>(`${this.apiUrl}/${idRencontre}/stats`);
}

// ✅ Initialiser le terrain
initialiserTerrain(idRencontre: number, vestiaireDTO: any): Observable<TerrainDTO> {
  return this.http.post<TerrainDTO>(`${this.apiUrl}/${idRencontre}/init/terrain`, vestiaireDTO);
}


// ✅ Gestion des remplacements
gererRemplacement(idRencontre: number, joueurSortantId: number, joueurEntrantId: number, minute: number): Observable<void> {
  const remplacementDTO = {
    idRencontre,
    idTitulaireSortant: joueurSortantId,
    remplaçant: { id: joueurEntrantId },
    minuteEntree: minute
  };

  return this.http.post<void>(`${this.apiUrl}/${idRencontre}/remplacement`, remplacementDTO);
}





}
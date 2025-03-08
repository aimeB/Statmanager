import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FeuilleDeMatchDTO } from '../models/feuille-de-match.model';
import { environment } from '../../../../environments/environment';







@Injectable({
  providedIn: 'root'
})
export class FeuilleDeMatchService {
  private apiUrl = `${environment.apiBaseUrl}/feuille-de-match`; // ✅ Endpoint correct

  constructor(private http: HttpClient) {}

  /**
   * 📌 **Met à jour les statistiques d'un joueur dans la feuille de match**
   * @param statDTO Objet contenant les nouvelles statistiques du joueur
   * @returns Observable de la requête HTTP
   */
  modifierStatistiquesJoueur(statDTO: FeuilleDeMatchDTO): Observable<any> {
    const url = `${this.apiUrl}/${statDTO.rid}/modifier-statistiques`;
  
    return this.http.put<any>(url, statDTO, { responseType: 'json' }); // ✅ Assure que `idPasseur` est inclus
}

  
}

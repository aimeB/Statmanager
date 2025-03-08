
import { FeuilleDeMatchDTO } from './../../feuille-de-match/models/feuille-de-match.model';

export interface TerrainDTO {
  idChampionnat: number;
  idRencontre: number;
  nomEquipe: string;
  nomAdversaire: string;
  butEquipe: number;
  butAdversaire: number;
  divisionAdversaire: string;
  titulaires: FeuilleDeMatchDTO[];  // ✅ Remplacement de Joueur par FeuilleDeMatchDTO
  remplacants: FeuilleDeMatchDTO[]; // ✅ Remplacement de Joueur par FeuilleDeMatchDTO
  terrainJoueurs: { [poste: string]: FeuilleDeMatchDTO }; // ✅ Mise à jour pour gérer les stats
  butsModifies: { [key: number]: number };
  passesModifies: { [key: number]: number };
  minutesJouees: { [key: number]: number };


  


}
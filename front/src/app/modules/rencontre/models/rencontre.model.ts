import { Division } from '@app/shared/models/division.enum';
import { StatutRencontre } from '@app/shared/models/statut-rencontre.enum';
import { JoueurDTO } from '@app/modules/joueur/models/joueur.model';
import { FeuilleDeMatchDTO } from '@app/modules/feuille-de-match/models/feuille-de-match.model';

/**
 * 📌 DTO représentant une Rencontre avec ses informations et feuilles de match associées.
 */
export interface RencontreDTO {
  rid: number;
  idChamp: number;
  nomEquipe: string;
  butEquipe: number;
  butAdversaire: number;
  nomAdversaire: string;
  divisionAdversaire: Division;
  hommeDuMatch?: string;
  statutRencontre: StatutRencontre;
  feuillesDeMatch?: FeuilleDeMatchDTO[];
}

/**
 * 📌 DTO léger pour afficher une liste simplifiée de rencontres.
 */
export interface RencontreLightDTO {
  rid: number;
  nomAdversaire: string;
  divisionAdversaire: Division;
  butAdversaire: number;
  statutRencontre: StatutRencontre;
}

/**
 * 📌 DTO contenant les détails d'une rencontre.
 */
export interface RencontreDetailDTO {
  rid: number;
  idChampionnat: number;
  nomEquipe: string;
  butEquipe: number;
  nomAdversaire: string;
  butAdversaire: number;
  divisionAdversaire: string;
  hommeDuMatch?: string;
  statutRencontre: StatutRencontre;
  statsJoueurs?: any[];
}

/**
 * 📌 DTO pour la gestion des événements d'un match.
 */
export interface EvenementMatchDTO {
  idRencontre: number;
  idJoueur: number;
  nomJoueur: string;
  minute: number;
  typeEvenement: string;
  idPasseur?: number;
  nomPasseur?: string;
}

/**
 * 📌 DTO pour le bilan d'une rencontre.
 */
export interface BilanRencontreDTO {
  totalRencontres: number;
  totalButsMarques: number;
  totalButsAdversaires: number;
  totalVictoires: number;
  totalDefaites: number;
  totalMatchsNuls: number;
}

/**
 * 📌 DTO pour la gestion des remplacements en temps réel.
 */
export interface RemplacementDTO {
  idRencontre: number;
  idRemplacantEntrant: number;
  idTitulaireSortant: number;
  minuteEntree: number;
}

/**
 * 📌 DTO utilisé pour finaliser une rencontre avant clôture.
 */
export interface ClotureRencontreDTO {
  idRencontre: number;
  nomAdversaire: string;
  butAdversaire: number;
  divisionAdversaire: Division;
  cotes: Record<number, number>;
  butsArretes: Record<number, number>;
}

/**
 * 📌 DTO utilisé pour la sélection des titulaires avant le match.
 */
export interface VestiaireDTO {
  idChampionnat: number;
  nomAdversaire: string;
  divisionAdversaire: Division;
  titulaires: JoueurDTO[];
  remplacants: JoueurDTO[];
}

/**
 * 📌 DTO pour la gestion du terrain en temps réel.
 */
export interface TerrainDTO {
  idChampionnat: number;
  idRencontre: number;
  nomEquipe: string;
  nomAdversaire: string;
  butEquipe: number;
  butAdversaire: number;
  divisionAdversaire: Division;
  titulaires: FeuilleDeMatchDTO[];
  remplacants: FeuilleDeMatchDTO[];
  terrainJoueurs: Record<string, FeuilleDeMatchDTO>;
  butsModifies: Record<number, number>;
  passesModifies: Record<number, number>;
  minutesJouees: Record<number, number>;
}

/**
 * 📌 DTO pour sélectionner une nouvelle rencontre après un match.
 */
export interface NouvelleRencontreSelectionDTO {
  idChampionnat: number;
  idDerniereRencontre: number;
  nomAdversaire: string;
  divisionAdversaire: Division;
  joueursPrecedents: JoueurDTO[];
  joueursDisponibles: JoueurDTO[];
}

export interface FinMatchDTO {
  idRencontre: number;
  equipeDomicile: string;
  scoreEquipe: number;
  equipeAdverse: string;
  scoreAdversaire: number;
  hommeDuMatch: string;
  statistiquesJoueurs: StatistiquesRencontreDTO[];
  evenementsMatch: EvenementMatchDTO[];
}

export interface StatistiquesRencontreDTO {
  idJoueur: number;
  nomJoueur: string;
  poste: string;
  buts: number;
  passes: number;
  minutesJouees: number;
  moyenneCote: number;
}

export interface EvenementMatchDTO {
  idRencontre: number;
  idJoueur: number;
  nomJoueur: string;
  minute: number;
  typeEvenement: string; // "BUT" ou "REMPLACEMENT"
  idPasseur?: number;
  nomPasseur?: string;
}

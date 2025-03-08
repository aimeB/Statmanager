/**
 * 📌 DTO représentant un joueur avec ses statistiques et sa position.
 */
export interface JoueurDTO {
  id: number;
  nom: string;
  poste: string;
  categoriePoste: string;
  buts: number;
  passes: number;
  minutesJouees: number;
  moyenneCote: number;
  point: number;
  typeJoueur: string;
}

/**
* 📌 DTO léger pour afficher une liste simplifiée de joueurs.
*/
export interface JoueurLightDTO {
  id: number;
  nom: string;
  poste: string;
}

/**
* 📌 DTO contenant les statistiques globales d'un joueur.
*/
export interface JoueurStatGlobalDTO {
  joueurId: number;
  nom: string;
  typeJoueur: string;
  poste: string;
  totalButsGlobal: number;
  totalPassesGlobal: number;
  totalMinutesJoueesGlobal: number;
  moyenneCoteGlobal: number;
  scoreOffensifGlobal: number;
  scoreRegulariteGlobal: number;
  scoreContributionGlobal: number;
  scoreEnduranceGlobal: number;
  performancesParDivision: Record<string, PerformanceParDivision>;
  derniersMatchs: any[];
}

/**
* 📌 DTO représentant la performance d'un joueur par division.
*/
export interface PerformanceParDivision {
  division: string;
  buts: number;
  passes: number;
  minutesJouees: number;
  moyenneCote: number;
  points: number;
}


export interface StatCritereDTO {
  joueurs: JoueurStatGlobalDTO[];
}


export interface StatTopJoueurDTO {
  meilleursJoueurs: JoueurStatGlobalDTO[];
}



export interface StatCompositeDTO {
  joueurs: JoueurStatGlobalDTO[];
}

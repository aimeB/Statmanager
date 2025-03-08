/**
 * 📌 DTO représentant les statistiques d'un joueur dans un championnat.
 */
export interface StatChampionnatDTO {
  id: number;
  joueurId: number;
  joueurNom: string;
  poste: string;
  categoriePoste: string;
  championnatId: number;
  butsChamp: number;
  passesChamp: number;
  minutesJoueesChamp: number;
  moyenneCoteChamp: number;
  pointsChamp: number;
}

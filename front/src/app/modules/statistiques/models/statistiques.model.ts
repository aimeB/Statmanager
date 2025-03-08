/**
 * 📌 DTO de base représentant les statistiques générales d'un joueur.
 */
export class StatistiquesDTO {
    joueurId!: number;
    nom!: string;
    typeJoueur!: string;
    poste!: string;
    buts: number = 0;
    passes: number = 0;
    cote: number = 5.0;
    points: number = 0;
    totalMinutesJouees: number = 0.0;
  
    getButsParMinute(): number {
      return this.totalMinutesJouees > 0 ? this.buts / this.totalMinutesJouees : 0;
    }
  
    getPassesParMinute(): number {
      return this.totalMinutesJouees > 0 ? this.passes / this.totalMinutesJouees : 0;
    }
  
    getPointsParMinute(): number {
      return this.totalMinutesJouees > 0 ? this.points / this.totalMinutesJouees : 0;
    }
  
    getMoyenneButsPasses(): number {
      return (this.getButsParMinute() + this.getPassesParMinute()) / 2;
    }
  }
  

/**
 * 📌 DTO contenant les statistiques d'un joueur dans un championnat spécifique.
 */
export interface StatistiquesChampionnatDTO extends StatistiquesDTO {
    championnatId: number;
    butsChamp: number;
    passesChamp: number;
    coteChamp: number;
    minutesJoueesChamp: number;
    pointsChamp: number;
}

/**
 * 📌 DTO contenant les statistiques d'un joueur pour une rencontre spécifique.
 */
export interface StatistiquesRencontreDTO extends StatistiquesDTO {
    rencontreId: number;
    minutesJouees: number;
    cote: number;
    idPasseurs: number[];
    nomPasseurs: string[];
}

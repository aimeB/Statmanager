import { RencontreDetailDTO } from "@app/modules/rencontre/models/rencontre.model";
import { Division } from "@app/shared/models/division.enum";
import { Statut } from "@app/shared/models/statut.enum";

/**
 * 📌 DTO pour la création d'un championnat avec les joueurs sélectionnés.
 */
export interface ChampionnatCreationDTO {
    division: Division;
    joueursIds: number[]; // Liste des 23 joueurs sélectionnés
}

/**
 * 📌 DTO représentant un Championnat avec ses informations essentielles.
 */
export interface ChampionnatDTO {
    idChamp: number;
    division: Division;
    statut: Statut;
    pointsActuels: number;
    pointsPromotion: number;
    pointsRelegation: number;
    nbrDeMatchJoue: number;
    rencontres?: RencontreDetailDTO[];
}

/**
 * 📌 DTO détaillé d'un championnat avec ses rencontres.
 */
export interface ChampionnatDetailWithRencontresDTO {
    idChamp: number;
    division: Division;
    statut: Statut;
    pointActuels: number;
    pointPromotion: number;
    pointRelegation: number;
    nbrDeMatchJoue: number;
    rencontres: RencontreDetailDTO[];
}

/**
 * 📌 DTO léger pour l'affichage rapide des championnats.
 */
export interface ChampionnatLightDTO {
    idChamp: number;
    division: Division;
    statut: Statut;
    pointsActuels: number;
    nombreRencontres: number;
}

/**
 * 📌 DTO pour l'aperçu des derniers championnats.
 */
export interface ChampionnatOverviewDTO {
    derniersChampionnats: ChampionnatLightDTO[];
    detailChampionnat?: ChampionnatDetailWithRencontresDTO;
}

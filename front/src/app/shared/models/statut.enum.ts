/**
 * 📌 Représente les différents statuts d'un championnat.
 * - ENCOURS : Le championnat est actuellement en cours.
 * - RELEGATION : L'équipe est dans la zone de relégation.
 * - MAINTIEN : L'équipe a assuré son maintien.
 * - PROMOTION : L'équipe est en position de promotion.
 */
export enum Statut {
    ENCOURS = "Championnat en cours",
    RELEGATION = "Relégation en cours",
    MAINTIEN = "Maintien assuré",
    PROMOTION = "Promotion en cours"
}

/**
 * 📌 Vérifie si le statut est positif.
 * @param statut Statut à vérifier.
 * @returns `true` si le statut est MAINTIEN ou PROMOTION, `false` sinon.
 */
export function isPositive(statut: Statut): boolean {
    return statut === Statut.PROMOTION || statut === Statut.MAINTIEN;
}

/**
 * 📌 Vérifie si le statut est négatif.
 * @param statut Statut à vérifier.
 * @returns `true` si le statut est RELEGATION, `false` sinon.
 */
export function isNegative(statut: Statut): boolean {
    return statut === Statut.RELEGATION;
}

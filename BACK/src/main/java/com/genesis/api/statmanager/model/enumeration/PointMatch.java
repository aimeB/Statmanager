package com.genesis.api.statmanager.model.enumeration;

/**
 * Enumération représentant les points attribués en fonction du résultat d'un match.
 */
public enum PointMatch {

    WIN(3),        // Victoire : 3 points
    DRAW(1),    // Égalité : 1 point
    LOST(0);       // Défaite : 0 point

    private final int value; // Valeur des points associés à chaque état

    PointMatch(int value) {
        this.value = value;
    }

    /**
     * Retourne la valeur des points.
     */
    public int getValue() {
        return value;
    }

    /**
     * Détermine le résultat d'un match en fonction des scores.
     *
     * @param butEquipe Nombre de buts marqués par l'équipe.
     * @param butAdversaire Nombre de buts marqués par l'adversaire.
     * @return Le résultat sous forme d'énumération PointMatch.
     */
    public static PointMatch determineResult(int butEquipe, int butAdversaire) {
        if (butEquipe > butAdversaire) {
            return WIN;
        } else if (butEquipe == butAdversaire) {
            return DRAW;
        } else {
            return LOST;
        }
    }
}


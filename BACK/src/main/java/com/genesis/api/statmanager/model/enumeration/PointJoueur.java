package com.genesis.api.statmanager.model.enumeration;

/**
 * Enumération représentant les points attribués aux joueurs à la fin du championnat
 * en fonction de leur classement.
 */
public enum PointJoueur {

    FIRST(10, "Premier"),
    SECOND(5, "Deuxième"),
    THIRD(3, "Troisième"),
    MOTION(1, "Mention honorable"),
    NULL(0, "Aucun point");

    private final int value; // Points attribués
    private final String description; // Description du classement

    /**
     * Constructeur pour l'énumération.
     *
     * @param value       Points attribués.
     * @param description Description du classement.
     */
    PointJoueur(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Retourne les points attribués.
     *
     * @return Points associés au classement.
     */
    public int getValue() {
        return value;
    }

    /**
     * Retourne la description du classement.
     *
     * @return Description associée au classement.
     */
    public String getDescription() {
        return description;
    }


    public static PointJoueur fromValue(int value) {
        for (PointJoueur point : PointJoueur.values()) {
            if (point.value == value) {
                return point;
            }
        }
        return PointJoueur.NULL; // ✅ Retourne une valeur par défaut au lieu de lever une exception
    }


    @Override
    public String toString() {
        return description + " (" + value + " points)";
    }
}

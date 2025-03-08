package com.genesis.api.statmanager.model.enumeration;
/**
        * Représente les différents statuts d'un championnat.
        * - ENCOURS : Le championnat est actuellement en cours.
        * - RELEGATION : L'équipe est dans la zone de relégation.
        * - MAINTIEN : L'équipe a assuré son maintien.
        * - PROMOTION : L'équipe est en position de promotion.
        */
public enum Statut {

    ENCOURS("Championnat en cours"),
    RELEGATION("Relégation en cours"),
    MAINTIEN("Maintien assuré"),
    PROMOTION("Promotion en cours");

    private final String description;

    /**
     * Constructeur de l'énumération avec une description.
     *
     * @param description Description lisible pour l'utilisateur.
     */
    Statut(String description) {
        this.description = description;
    }

    /**
     * Récupère la description du statut.
     *
     * @return Description lisible du statut.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Vérifie si le statut est positif.
     *
     * @return {@code true} si le statut est MAINTIEN ou PROMOTION, {@code false} sinon.
     */
    public boolean isPositive() {
        return this == PROMOTION || this == MAINTIEN;
    }

    /**
     * Vérifie si le statut est négatif.
     *
     * @return {@code true} si le statut est RELEGATION, {@code false} sinon.
     */
    public boolean isNegative() {
        return this == RELEGATION;
    }
}

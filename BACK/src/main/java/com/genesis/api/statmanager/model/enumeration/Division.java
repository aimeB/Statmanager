package com.genesis.api.statmanager.model.enumeration;

import java.util.*;

/**
 * Représente les divisions dans le championnat.
 * Fournit des méthodes pour naviguer entre les divisions de manière cyclique.
 */
public enum Division {

    DIV5("Division 5 - Débutants"),
    DIV4("Division 4 - Joueurs intermédiaires"),
    DIV3("Division 3 - Avancés"),
    DIV2("Division 2 - Compétiteurs"),
    DIV1("Division 1 - Professionnels");

    private final String description;

    Division(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }



    // Set ordonné pour gérer les divisions
    private static final NavigableSet<Division> set =
            new TreeSet<>(EnumSet.allOf(Division.class));

    /**
     * Retourne la division suivante dans l'ordre naturel.
     * Si c'est la dernière division, retourne la première (cycle).
     *
     * @return Division suivante.
     */
    public Division next() {
        return Objects.requireNonNullElseGet(
                set.higher(this), set::first
        );
    }

    /**
     * Retourne la division précédente dans l'ordre naturel.
     * Si c'est la première division, retourne la dernière (cycle).
     *
     * @return Division précédente.
     */
    public Division previous() {
        return Objects.requireNonNullElseGet(
                set.lower(this), set::last
        );
    }

    /**
     * Convertit une chaîne de caractères en `Division`, avec gestion des erreurs.
     *
     * @param divisionStr La chaîne à convertir
     * @return La `Division` correspondante ou `DIV5` par défaut
     */
    public static Division fromString(String divisionStr) {
        if (divisionStr == null || divisionStr.isBlank()) {
            return DIV5; // ✅ Valeur par défaut si la chaîne est vide ou null
        }
        return Arrays.stream(Division.values())
                .filter(division -> division.name().equalsIgnoreCase(divisionStr.trim())
                        || division.getDescription().equalsIgnoreCase(divisionStr.trim()))
                .findFirst()
                .orElse(DIV5); // ✅ Valeur par défaut si aucune correspondance
    }
}



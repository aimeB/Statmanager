package com.genesis.api.statmanager.model.enumeration;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

import static com.genesis.api.statmanager.model.enumeration.CategoriePoste.INCONNU;

public enum Poste {
    // ✅ Poste inconnu pour gérer les cas d'erreur
    INCONNU("Poste Inconnu", CategoriePoste.INCONNU),

    // Postes des gardiens
    GB("Gardien", CategoriePoste.GARDIEN),

    // Postes des défenseurs
    DC_GAUCHE("Défenseur Central Gauche", CategoriePoste.DEFENSEUR),
    DC_DROIT("Défenseur Central Droit", CategoriePoste.DEFENSEUR),
    DG("Défenseur Gauche", CategoriePoste.DEFENSEUR),
    DD("Défenseur Droit", CategoriePoste.DEFENSEUR),

    // Postes des milieux
    MDF("Milieu Défensif", CategoriePoste.MILIEU),
    MR("Milieu Relayeur", CategoriePoste.MILIEU),
    MLG("Milieu Latéral Gauche", CategoriePoste.MILIEU),
    MLD("Milieu Latéral Droit", CategoriePoste.MILIEU),
    MO("Milieu Offensif", CategoriePoste.MILIEU),

    // Postes des attaquants
    AC("Avant-centre", CategoriePoste.ATTAQUANT),
    AIG("Ailier Gauche", CategoriePoste.ATTAQUANT),
    AID("Ailier Droit", CategoriePoste.ATTAQUANT),
    SA("Second Attaquant", CategoriePoste.ATTAQUANT);

    private final String description;
    private final CategoriePoste categoriePoste;

    Poste(String description, CategoriePoste categoriePoste) {
        this.description = description;
        this.categoriePoste = categoriePoste;
    }

    public String getDescription() {
        return description;
    }

    public CategoriePoste getCategoriePoste() {
        return categoriePoste;
    }

    // ✅ Vérifie si le poste est défensif
    public boolean isDefensif() {
        return this.categoriePoste == CategoriePoste.DEFENSEUR || this == MDF;
    }

    // ✅ Vérifie si le poste est un milieu
    public boolean isMilieu() {
        return this.categoriePoste == CategoriePoste.MILIEU;
    }

    // ✅ Vérifie si le poste est offensif
    public boolean isOffensif() {
        return this.categoriePoste == CategoriePoste.ATTAQUANT || this == MO;
    }

    public boolean isGardien() {
        return this.categoriePoste == CategoriePoste.GARDIEN;
    }


    // ✅ Conversion sécurisée String -> Poste avec gestion des erreurs
    public static Poste fromString(String poste) {
        if (poste == null || poste.isBlank()) {
            return Poste.INCONNU;
        }
        return Arrays.stream(Poste.values())
                .filter(p -> p.name().equalsIgnoreCase(poste.trim()))
                .findFirst()
                .orElseGet(() -> {
                    System.err.println("⚠️ Poste inconnu détecté : " + poste);
                    return Poste.INCONNU;
                });
    }

}

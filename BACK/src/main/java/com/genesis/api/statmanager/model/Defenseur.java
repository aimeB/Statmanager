package com.genesis.api.statmanager.model;

import com.genesis.api.statmanager.model.enumeration.PointJoueur;
import com.genesis.api.statmanager.model.enumeration.Poste;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("Defenseur")
@NoArgsConstructor
public class Defenseur extends Joueur {

    public Defenseur(Long id, String nom, Poste poste) {
        super(id, nom, poste);
    }

    public Defenseur(Long jid, String nom, Poste poste, int totalButs, int totalPasses, double totalMinutesJouees, double moyenneCote, int points) {
        super(jid, nom, poste, totalButs, totalPasses, totalMinutesJouees, moyenneCote, points);
    }
    // Pas de champs supplémentaires
}

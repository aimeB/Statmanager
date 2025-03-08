package com.genesis.api.statmanager.model;

import com.genesis.api.statmanager.model.enumeration.Poste;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("Attaquant")
@NoArgsConstructor
public class Attaquant extends Joueur {

    public Attaquant(Long id, String nom, Poste poste) {
        super(id, nom, poste);
    }

    // ✅ Ajout du constructeur avec `point`
    public Attaquant(Long jid, String nom, Poste poste, int totalButs, int totalPasses, double totalMinutesJouees, double moyenneCote, int point) {
        super(jid, nom, poste, totalButs, totalPasses, totalMinutesJouees, moyenneCote, point);
    }
}

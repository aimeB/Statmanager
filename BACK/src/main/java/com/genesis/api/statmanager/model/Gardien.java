package com.genesis.api.statmanager.model;

import com.genesis.api.statmanager.model.enumeration.PointJoueur;
import com.genesis.api.statmanager.model.enumeration.Poste;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("Gardien")
@NoArgsConstructor
public class Gardien extends Joueur {

    public Gardien(Long id, String nom, Poste poste) {
        super(id, nom, poste);
    }

}


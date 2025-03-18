package com.genesis.api.statmanager.projection;

import com.genesis.api.statmanager.model.enumeration.Poste;

public interface JoueurProjection {
    Long getJid();
    String getNom();
    String getPoste();
    int getTotalButs();
    int getTotalPasses();
    double getTotalMinutesJouees();
    double getTotalMoyenneCote();
    String getTypeJoueur();

}

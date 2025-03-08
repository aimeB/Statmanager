package com.genesis.api.statmanager.projection;

import com.genesis.api.statmanager.model.enumeration.Poste;

public interface FeuilleDeMatchProjection {
    Long getId();
    Long getRid();
    Long getJid();
    String getNom();
    String getPoste();  // ✅ Doit correspondre à `AS poste` dans la requête
    int getButs();
    int getPasses();
    double getMoyenneCote();
    double getMinutesJouees();
    Boolean getAJoue();
    boolean isTitulaire();
    int getButArreter();
    int getButEncaisser();
    String getPasseursIds();
    String getNomsPasseurs();
}

package com.genesis.api.statmanager.model;

import com.genesis.api.statmanager.model.Joueur;

public class JoueurProxy extends Joueur {
    public JoueurProxy(Long jid) {
        this.setJid(jid); // ✅ Seule l'ID est stockée, pas d'instanciation complète !
    }
}

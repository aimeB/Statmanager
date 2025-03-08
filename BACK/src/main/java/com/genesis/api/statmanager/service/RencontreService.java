package com.genesis.api.statmanager.service;

import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.dto.rencontre.*;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * 📌 Service de gestion des rencontres (matchs).
 */
public interface RencontreService {

    // 📌 1️⃣ GESTION DES RENCONTRES
    void supprimerRencontre(Long idRencontre);
    List<RencontreDTO> findByChampionnat(Long idChampionnat);
    List<RencontreDTO> findRencontresTerminees();
    RencontreDetailDTO getRencontreDetailsStat(Long idRencontre);
    BilanRencontreDTO getBilanRencontresParChampionnat(Long idChampionnat);

    // 📌 2️⃣ SÉLECTION ET PRÉPARATION DES RENCONTRES
    NouvelleRencontreSelectionDTO getNouvelleRencontreSelection(Long idChampionnat);
    TerrainDTO validerSelectionVestiaire(VestiaireDTO vestiaireDTO);
    TerrainDTO creerNouvelleRencontre(VestiaireDTO vestiaireDTO);

    // 📌 3️⃣ GESTION DU TERRAIN ET MATCH EN COURS
    TerrainDTO initialiserTerrain(Long idRencontre );
    TerrainDTO getTerrain(Long idRencontre);
    TerrainDTO updateStatsEnTempsReel(Long idRencontre, Long idJoueur, int buts, Long idPasseur);
    TerrainDTO effectuerRemplacement(RemplacementDTO remplacementDTO);

    // 📌 4️⃣ FIN DE MATCH ET MISE À JOUR DES STATS
    FinMatchDTO cloturerRencontre(ClotureRencontreDTO clotureDTO);
    List<EvenementMatchDTO> getHistoriqueEvenements(Long idRencontre);

    // 📌 5️⃣ VÉRIFICATIONS ET MISES À JOUR TRANSACTIONNELLES
    @Transactional
    void updateButsTransactionnel(Long idFeuilleMatch, int buts, int passes);
    @Transactional
    void updatePassesTransactionnel(Long idFeuilleMatch, int passes);

    List<StatistiquesRencontreDTO> getStatistiquesRencontre(Long idRencontre);
}
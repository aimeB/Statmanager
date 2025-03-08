package com.genesis.api.statmanager.service;

import com.genesis.api.statmanager.dto.global.StatistiquesDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurLightDTO;
import com.genesis.api.statmanager.dto.joueur.JoueurStatGlobalDTO;
import com.genesis.api.statmanager.dto.statistique.StatCompositeDTO;
import com.genesis.api.statmanager.dto.statistique.StatCritereDTO;
import com.genesis.api.statmanager.dto.statistique.StatTopJoueurDTO;

import java.util.List;

/**
 * 📌 Service de gestion des joueurs.
 */
public interface JoueurService {

    /**
     * 📌 Récupère tous les joueurs sous forme de DTO léger.
     *
     * @return Liste de JoueurDTO.
     */
    List<JoueurDTO> findAllAsDTO();

    /**
     * 📌 Récupère un joueur précis (DTO) par ID.
     *
     * @param id ID du joueur.
     * @return JoueurDTO.
     */
    JoueurDTO findByIdAsDTO(Long id);

    /**
     * 📌 Récupère une liste allégée des joueurs (ID, Nom, Poste) triée par poste.
     *
     * @return Liste de JoueurDTO (light).
     */
    List<JoueurLightDTO> getJoueursLight();

    /**
     * 📌 Récupère un joueur avec ses statistiques globales (buts, passes, minutes, cote...).
     *
     * @param id ID du joueur.
     * @return JoueurStatGlobalDTO.
     */
    JoueurStatGlobalDTO findByIdWithGlobalStats(Long id);

    /**
     * 📌 Récupère tous les joueurs avec leurs statistiques globales.
     *
     * @return Liste de JoueurStatGlobalDTO.
     */
    List<JoueurStatGlobalDTO> findAllWithGlobalStats();

    /**
     * 📌 Classe et retourne les meilleurs joueurs selon un critère (ex: buts, passes) et/ou par poste.
     *
     * @param critere Critère de tri (ex: "buts", "passes", "scoreOffensifGlobal", etc.).
     * @param poste   Filtre sur un poste spécifique (ex: "AC"), ou null pour ne pas filtrer.
     * @return Liste (max 10) des meilleurs joueurs correspondant au critère.
     */
    List<JoueurStatGlobalDTO> getTopJoueursGlobaux(String critere, String poste);

    /**
     * 📌 Retourne les joueurs triés selon un critère spécifique.
     */
    StatCritereDTO getJoueursParCritere(String critere);

    /**
     * 📌 Retourne les 5 meilleurs joueurs des dernières rencontres.
     */
    StatTopJoueurDTO getMeilleursJoueurs();

    /**
     * 📌 Retourne les joueurs triés selon un score composite.
     */
    StatCompositeDTO getJoueursParScoreComposite();

    List<StatistiquesDTO> getAllJoueursStats();

    JoueurStatGlobalDTO findByNomWithGlobalStats(String nom);
}

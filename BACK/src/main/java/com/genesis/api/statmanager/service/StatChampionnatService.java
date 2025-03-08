package com.genesis.api.statmanager.service;

import com.genesis.api.statmanager.dto.StatChampionnatDTO;
import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import java.util.List;
import java.util.Optional;

/**
 * 📌 Service de gestion des statistiques d'un joueur dans un championnat donné.
 */
public interface StatChampionnatService {

    /**
     * 📌 Récupère toutes les statistiques championnat sous forme de DTO.
     *
     * @return Liste de StatChampionnatDTO.
     */
    List<StatChampionnatDTO> findAllAsDTO();

    /**
     * 📌 Récupère les statistiques d'un joueur pour un championnat précis.
     *
     * @param joueurId      ID du joueur.
     * @param championnatId ID du championnat.
     * @return Optional<StatChampionnatDTO>.
     */
    Optional<StatChampionnatDTO> findByJoueurAndChampionnatAsDTO(Long joueurId, Long championnatId);

    /**
     * 📌 Récupère toutes les statistiques de championnat d'un joueur,
     *     peu importe le championnat.
     *
     * @param joueurId ID du joueur.
     * @return Liste de StatChampionnatDTO.
     */
    List<StatChampionnatDTO> findByJoueurAsDTO(Long joueurId);

    /**
     * 📌 Récupère toutes les statistiques liées à un championnat donné.
     *
     * @param championnatId ID du championnat.
     * @return Liste de StatChampionnatDTO.
     */
    List<StatChampionnatDTO> findByChampionnatAsDTO(Long championnatId);

    /**
     * 📌 Récupère le classement des meilleurs joueurs dans un championnat
     *     selon un critère précis (buts, passes, cote...).
     *
     * @param idChampionnat ID du championnat.
     * @param critere       Le critère de tri : "buts", "passes", "cote", etc.
     * @return Liste (max 10) triée.
     */
    List<StatChampionnatDTO> getTopJoueurs(Long idChampionnat, String critere);

    /**
     * 🔄 Met à jour les statistiques de plusieurs joueurs après une rencontre.
     *
     * @param idChampionnat   ID du championnat concerné.
     * @param statsRencontres Liste des stats de la rencontre (buts, passes, minutes...).
     */
    void mettreAJourStatistiques(Long idChampionnat, List<StatistiquesRencontreDTO> statsRencontres);
}

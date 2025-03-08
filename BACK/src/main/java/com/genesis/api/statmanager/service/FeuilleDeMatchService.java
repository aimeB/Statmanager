package com.genesis.api.statmanager.service;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.model.FeuilleDeMatch;

import java.util.List;

/**
 * 📌 Service de gestion des feuilles de match.
 */
public interface FeuilleDeMatchService {

    /**
     * 📌 Récupère les feuilles de match d’une rencontre.
     *
     * @param rencontreId ID de la rencontre.
     * @return Liste de FeuilleDeMatchDTO.
     */
    List<FeuilleDeMatchDTO> findByRencontre(Long rencontreId);

    /**
     * 📌 Récupère la feuille de match spécifique d’un joueur dans une rencontre.
     *
     * @param rencontreId ID de la rencontre.
     * @param joueurId    ID du joueur.
     * @return FeuilleDeMatchDTO correspondant.
     */
    FeuilleDeMatchDTO findByRencontreAndJoueur(Long rencontreId, Long joueurId);

}

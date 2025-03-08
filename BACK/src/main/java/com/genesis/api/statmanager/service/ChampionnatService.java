package com.genesis.api.statmanager.service;

import com.genesis.api.statmanager.dto.championnat.*;
import com.genesis.api.statmanager.dto.global.StatistiquesChampionnatDTO;
import com.genesis.api.statmanager.model.Championnat;
import com.genesis.api.statmanager.model.enumeration.Division;
import java.util.List;

/**
 * 📌 Service pour la gestion des championnats et leurs statistiques.
 */
public interface ChampionnatService {

    /**
     * 📌 Crée un championnat en division donnée avec les joueurs indiqués, puis initialise leurs statistiques.
     *
     * @param division   Division du championnat (DIV1, DIV2, etc.)
     * @param joueursIds Liste des IDs de joueurs à inclure (exactement 23 joueurs).
     * @return ChampionnatDTO créé.
     */
    ChampionnatDTO creerChampionnat(Division division, List<Long> joueursIds);

    /**
     * 📌 Supprime un championnat.
     *
     * @param idChamp ID du championnat à supprimer.
     */
    void supprimerChampionnat(Long idChamp);

    /**
     * 📌 Récupère les 10 derniers championnats.
     *
     * @return Liste de ChampionnatDTO (max 10).
     */
    List<ChampionnatLightDTO> findTop10ByOrderByIdChampDesc(); // ✅ Change le type de retour ici


    void cloturerChampionnat(Long idChampionnat);


    /**
     * 📌 Récupère un championnat avec ses rencontres.
     *
     * @param idChamp ID du championnat.
     * @return ChampionnatDetailWithRencontresDTO.
     */
    ChampionnatDetailWithRencontresDTO findChampionnatWithRencontres(Long idChamp);

    /**
     * 📌 Récupère le récapitulatif (overview) d'un championnat, incluant la liste des derniers championnats.
     *
     * @param idChamp Identifiant du championnat sélectionné.
     * @return ChampionnatOverviewDTO (liste des derniers + détails du sélectionné).
     */
    ChampionnatOverviewDTO getChampionnatOverview(Long idChamp);

    /**
     * 📌 Récupère les statistiques d'un championnat (buts, passes, cotes, etc.).
     *
     * @param idChampionnat L'ID du championnat.
     * @return Liste de StatistiquesChampionnatDTO.
     */
    List<StatistiquesChampionnatDTO> getStatistiquesChampionnat(Long idChampionnat);

    void verifierStatutChampionnat(Championnat championnat);
}

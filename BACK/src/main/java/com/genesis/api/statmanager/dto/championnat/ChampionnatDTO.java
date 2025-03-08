package com.genesis.api.statmanager.dto.championnat;

import com.genesis.api.statmanager.dto.rencontre.RencontreDTO;
import com.genesis.api.statmanager.model.enumeration.Division;
import com.genesis.api.statmanager.model.enumeration.Statut;
import lombok.*;

import java.util.List;

/**
 * 📌 DTO représentant un Championnat avec ses informations essentielles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChampionnatDTO {

    private Long idChamp; // ✅ ID du championnat
    private Division division; // ✅ Division (ENUM : DIV1, DIV2, etc.)
    private Statut statut; // ✅ Statut du championnat (ENCOURS, PROMOTION, RELÉGATION)

    private int pointsActuels; // ✅ Points actuels du championnat
    private int pointsPromotion; // ✅ Points nécessaires pour promotion
    private int pointsRelegation; // ✅ Points seuil pour éviter relégation

    private int nbrDeMatchJoue; // ✅ Nombre de matchs joués

    private List<RencontreDTO> rencontres; // ✅ Liste des rencontres associées (peut être chargée séparément)

    /**
     * ✅ Constructeur sans la liste des rencontres (pour chargement rapide).
     */
    public ChampionnatDTO(Long idChamp, Division division, Statut statut,
                          int pointsActuels, int pointsPromotion, int pointsRelegation, int nbrDeMatchJoue) {
        this.idChamp = idChamp;
        this.division = division;
        this.statut = statut;
        this.pointsActuels = pointsActuels;
        this.pointsPromotion = pointsPromotion;
        this.pointsRelegation = pointsRelegation;
        this.nbrDeMatchJoue = nbrDeMatchJoue;
    }
}

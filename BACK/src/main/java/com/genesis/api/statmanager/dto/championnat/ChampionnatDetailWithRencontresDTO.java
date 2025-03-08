package com.genesis.api.statmanager.dto.championnat;

import com.genesis.api.statmanager.dto.global.StatistiquesChampionnatDTO;
import com.genesis.api.statmanager.dto.rencontre.RencontreDetailDTO;
import com.genesis.api.statmanager.model.enumeration.Division;
import com.genesis.api.statmanager.model.enumeration.Statut;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionnatDetailWithRencontresDTO {
    private Long idChamp;                        // ID du championnat
    private Division division;                     // ✅ Division sous forme de String
    private Statut statut;                       // ✅ Statut sous forme de String
    private int pointActuels;                    // Points actuels du championnat
    private int pointPromotion;                  // Points requis pour la promotion
    private int pointRelegation;                 // Points requis pour le maintien
    private int nbrDeMatchJoue;                  // Nombre de matchs joués
    private List<RencontreDetailDTO> rencontres; // Liste des rencontres du championnat avec leurs détails
}

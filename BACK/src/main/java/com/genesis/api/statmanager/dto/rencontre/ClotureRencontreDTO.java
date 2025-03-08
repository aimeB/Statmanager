package com.genesis.api.statmanager.dto.rencontre;

import com.genesis.api.statmanager.model.enumeration.Division;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 📌 DTO utilisé pour finaliser une rencontre avant clôture.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClotureRencontreDTO {
    private Long idRencontre;
    private String nomAdversaire;
    private int butAdversaire;
    private Division divisionAdversaire;
    private Map<Long, Double> cotes;  // ✅ Cotes finales des joueurs
    private Map<Long, Integer> butsArretes; // ✅ Nombre de buts arrêtés des gardiens (ajouté)
}


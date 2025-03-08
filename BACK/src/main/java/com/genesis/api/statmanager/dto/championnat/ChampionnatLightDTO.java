package com.genesis.api.statmanager.dto.championnat;

import com.genesis.api.statmanager.model.enumeration.Division;
import com.genesis.api.statmanager.model.enumeration.Statut;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChampionnatLightDTO {
    private Long idChamp;
    private Division division;
    private Statut statut;
    private Integer pointsActuels;
    private Integer nombreRencontres; // ✅ Doit être Integer car COUNT() retourne Long par défaut

    public ChampionnatLightDTO(Long idChamp, Division division, Statut statut, Integer pointsActuels, Integer nombreRencontres) {
        this.idChamp = idChamp;
        this.division = division;
        this.statut = statut;
        this.pointsActuels = pointsActuels;
        this.nombreRencontres = nombreRencontres;
    }
}

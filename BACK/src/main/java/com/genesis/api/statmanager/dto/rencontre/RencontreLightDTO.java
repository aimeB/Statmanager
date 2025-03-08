package com.genesis.api.statmanager.dto.rencontre;

import com.genesis.api.statmanager.model.enumeration.Division;
import com.genesis.api.statmanager.model.enumeration.StatutRencontre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RencontreLightDTO {
    private Long rid;
    private String nomAdversaire;
    private Division divisionAdversaire;
    private int butAdversaire;
    private StatutRencontre statutRencontre;



    public RencontreLightDTO(Long rid, String nomAdversaire, Division divisionAdversaire, int butAdversaire, StatutRencontre statutRencontre) {
        this.rid = rid;
        this.nomAdversaire = nomAdversaire;
        this.divisionAdversaire = divisionAdversaire;
        this.butAdversaire = butAdversaire;
        this.statutRencontre = statutRencontre;
    }
}




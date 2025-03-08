package com.genesis.api.statmanager.dto.rencontre;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BilanRencontreDTO {
    private Long totalRencontres;
    private Long totalButsMarques;
    private Long totalButsAdversaires;
    private Long totalVictoires;
    private Long totalDefaites;
    private Long totalMatchsNuls;

    /**
     * ✅ Calcul du différentiel de buts.
     */
    public Long getDifferenceButs() {
        return totalButsMarques - totalButsAdversaires;
    }

    /**
     * ✅ Calcul du ratio de victoires (exprimé en pourcentage).
     */
    public double getRatioVictoires() {
        return (totalRencontres > 0) ? (totalVictoires * 100.0 / totalRencontres) : 0;
    }
}

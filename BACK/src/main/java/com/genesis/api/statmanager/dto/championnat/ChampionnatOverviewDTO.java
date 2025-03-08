package com.genesis.api.statmanager.dto.championnat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionnatOverviewDTO {
    private List<ChampionnatLightDTO> derniersChampionnats; // Liste des 10 derniers championnats
    private ChampionnatDetailWithRencontresDTO detailChampionnat; // Détail d'un championnat sélectionné avec rencontres
}

package com.genesis.api.statmanager.dto.championnat;

import com.genesis.api.statmanager.model.enumeration.Division;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionnatCreationDTO {
    private Division division;
    private List<Long> joueursIds; // Liste des 23 joueurs sélectionnés
}


package com.genesis.api.statmanager.dto.statistique;

import com.genesis.api.statmanager.dto.joueur.JoueurStatGlobalDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * * 📌 DTO pour identifier les meilleurs joueurs des 5 dernières rencontres
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatTopJoueurDTO {
    private List<JoueurStatGlobalDTO> joueurs;
}

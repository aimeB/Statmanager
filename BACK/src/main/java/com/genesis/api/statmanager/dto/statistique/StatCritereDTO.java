package com.genesis.api.statmanager.dto.statistique;

import com.genesis.api.statmanager.dto.joueur.JoueurStatGlobalDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 📌 DTO pour le tri des joueurs par critère spécifique (ex: buts, passes, minutes jouées, moyenne de cote)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatCritereDTO {
    private List<JoueurStatGlobalDTO> joueurs;
}

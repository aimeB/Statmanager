package com.genesis.api.statmanager.dto.statistique;

import com.genesis.api.statmanager.dto.joueur.JoueurStatGlobalDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 📌 DTO pour le tri composite des joueurs selon plusieurs critères pondérés
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatCompositeDTO {
    private List<JoueurStatGlobalDTO> joueurs;
}

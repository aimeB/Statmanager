package com.genesis.api.statmanager.dto.rencontre;

import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.model.enumeration.Division;
import lombok.*;

import java.util.List;

/**
 * 📌 DTO utilisé pour la sélection des titulaires et la préparation de la rencontre avant le terrain.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VestiaireDTO {
    private Long idChampionnat;                        // ID du championnat
    private String nomAdversaire;                      // Nom de l'équipe adverse
    private Division divisionAdversaire;               // Division adverse
    private List<JoueurDTO> titulaires;               // ✅ Liste des titulaires sélectionnés
    private List<JoueurDTO> remplacants;              // ✅ Liste des remplaçants
}

package com.genesis.api.statmanager.dto.rencontre;

import com.genesis.api.statmanager.dto.joueur.JoueurDTO;
import com.genesis.api.statmanager.model.enumeration.Division;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NouvelleRencontreSelectionDTO {
    private Long idChampionnat;
    private Long idDerniereRencontre;               // ID de la dernière rencontre jouée
    private String nomAdversaire;
    private Division divisionAdversaire;
    private List<JoueurDTO> joueursPrecedents;      // Joueurs ayant joué la dernière rencontre
    private List<JoueurDTO> joueursDisponibles;     // Liste complète des joueurs du championnat
}

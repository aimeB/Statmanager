package com.genesis.api.statmanager.dto.rencontre;

import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.dto.rencontre.EvenementMatchDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 📌 DTO pour représenter l'écran de fin de match
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinMatchDTO {
    private Long idRencontre;
    private String nomEquipe;
    private int butEpique;
    private String nomAdversaire;
    private int butAdversaire;
    private String hommeDuMatch;
    private List<StatistiquesRencontreDTO> statistiquesJoueurs;
    private List<EvenementMatchDTO> evenementsMatch; // ✅ Ajout des événements du match (buts, passes, remplacements)
}

package com.genesis.api.statmanager.dto.rencontre;

import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.model.enumeration.StatutRencontre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RencontreDetailDTO {
    private Long rid;                     // ID de la rencontre
    private Long idChampionnat;           // ID du championnat
    private String nomEquipe;       // Nom de l'équipe locale (ex: "HERSTAL FC")
    private int butEquipe;          // Buts marqués par l'équipe locale
    private String nomAdversaire;         // Nom de l'équipe adverse
    private int butAdversaire;            // Buts marqués par l'adversaire
    private String divisionAdversaire;       // Division de l'adversaire
    private Long hommeDuMatchId; // ✅ Stocke l'ID du joueur élu homme du match
    private String hommeDuMatchNom; // ✅ Stocke le nom du joueur élu
       // Joueur désigné homme du match
    private StatutRencontre statutRencontre; // Statut de la rencontre (ENCOURS, TERMINE, etc.)
    private List<StatistiquesRencontreDTO> statsJoueurs; // Liste des stats des joueurs pour ce match
}

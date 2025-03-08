package com.genesis.api.statmanager.dto.rencontre;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.model.Rencontre;
import com.genesis.api.statmanager.model.enumeration.Division;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 📌 DTO central pour gérer les feuilles de match, les joueurs et les scores en temps réel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerrainDTO {

    private Long idChampionnat; // ✅ ID du championnat
    private Long idRencontre;   // ✅ ID de la rencontre

    private String nomEquipe = "HERSTAL FC"; // ✅ Nom de l'équipe locale (écrit en dur)
    private String nomAdversaire;  // ✅ Nom de l'adversaire

    private int butEquipe = 0; // ✅ Score de l'équipe locale (calculé dynamiquement)
    private int butAdversaire = 0;   // ✅ Score de l'adversaire (modifiable par l'utilisateur)

    private Division divisionAdversaire; // ✅ Division de l'adversaire

    private List<FeuilleDeMatchDTO> titulaires;  // ✅ Liste des joueurs titulaires avec leurs stats du match
    private List<FeuilleDeMatchDTO> remplacants; // ✅ Liste des remplaçants avec leurs stats du match
    private Map<String, FeuilleDeMatchDTO> terrainJoueurs; // ✅ Stockage des feuilles de match affectées à chaque poste

    // ✅ Stockage des stats **modifiables AVANT validation**
    private Map<Long, Integer> butsModifies;   // 📌 Buts modifiés par joueur
    private Map<Long, Integer> passesModifies; // 📌 Passes modifiées par joueur
    private Map<Long, Double> minutesJouees;   // 📌 Minutes jouées (format `double`)



    public TerrainDTO(Rencontre rencontre, List<FeuilleDeMatchDTO> feuillesDeMatch) {
        this.idChampionnat = rencontre.getChampionnat().getIdChamp();
        this.idRencontre = rencontre.getRid();
        this.nomAdversaire = rencontre.getNomAdversaire();
        this.divisionAdversaire = rencontre.getDivisionAdversaire();
        this.butAdversaire = rencontre.getButAdversaire();

        // ✅ Initialisation des joueurs à partir des feuilles de match
        this.titulaires = feuillesDeMatch.stream()
                .filter(FeuilleDeMatchDTO::isTitulaire)
                .toList();

        this.remplacants = feuillesDeMatch.stream()
                .filter(feuille -> !feuille.isTitulaire())
                .toList();
    }

}

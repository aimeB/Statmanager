package com.genesis.api.statmanager.dto.rencontre;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.model.enumeration.Division;
import com.genesis.api.statmanager.model.enumeration.StatutRencontre;
import lombok.*;

import java.util.List;

/**
 * 📌 DTO représentant une Rencontre avec ses informations et feuilles de match associées.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RencontreDTO {

    private Long rid; // ✅ ID de la rencontre
    private Long idChamp; // ✅ ID du championnat associé

    private String nomEquipe = "Herstal FC"; // ✅ Nom de l'équipe locale (fixe)
    private int butEquipe; // ✅ Score de Herstal FC
    private int butAdversaire; // ✅ Score de l'adversaire
    private String nomAdversaire; // ✅ Nom de l'adversaire
    private Division divisionAdversaire; // ✅ Division de l'adversaire

    private String hommeDuMatch; // ✅ Joueur élu homme du match
    private StatutRencontre statutRencontre; // ✅ Statut de la rencontre (EN ATTENTE, TERMINEE, etc.)

    private List<FeuilleDeMatchDTO> feuillesDeMatch; // ✅ Feuilles de match associées à la rencontre

    /**
     * ✅ Constructeur simplifié pour récupérer une rencontre sans charger toutes les feuilles de match.
     */
    public RencontreDTO(Long rid, Long idChamp, int butEquipe, String nomAdversaire,
                        int butAdversaire, Division divisionAdversaire, String hommeDuMatch, StatutRencontre statutRencontre) {
        this.rid = rid;
        this.idChamp = idChamp;
        this.nomEquipe = "Herstal FC"; // ✅ Fixé en dur
        this.butEquipe = butEquipe;
        this.nomAdversaire = nomAdversaire;
        this.butAdversaire = butAdversaire;
        this.divisionAdversaire = divisionAdversaire;
        this.hommeDuMatch = hommeDuMatch;
        this.statutRencontre = statutRencontre;
    }

    /**
     * ✅ Constructeur utilisé **après la création de la rencontre**, incluant les feuilles de match.
     */
    public RencontreDTO(Long rid, Long idChamp, int butEquipe, String nomAdversaire,
                        int butAdversaire, Division divisionAdversaire, String hommeDuMatch,
                        StatutRencontre statutRencontre, List<FeuilleDeMatchDTO> feuillesDeMatch) {
        this.rid = rid;
        this.idChamp = idChamp;
        this.nomEquipe = "Herstal FC";
        this.butEquipe = butEquipe;
        this.nomAdversaire = nomAdversaire;
        this.butAdversaire = butAdversaire;
        this.divisionAdversaire = divisionAdversaire;
        this.hommeDuMatch = hommeDuMatch;
        this.statutRencontre = statutRencontre;
        this.feuillesDeMatch = feuillesDeMatch; // ✅ Liste complète des feuilles de match après création
    }
}

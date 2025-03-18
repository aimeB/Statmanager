package com.genesis.api.statmanager.dto;

import com.genesis.api.statmanager.model.FeuilleDeMatch;
import com.genesis.api.statmanager.model.enumeration.Poste;
import com.genesis.api.statmanager.model.enumeration.TimePlay;
import com.genesis.api.statmanager.projection.FeuilleDeMatchProjection;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 📌 DTO représentant une Feuille de Match d'un joueur.
 */
@Data
@NoArgsConstructor
@Builder
public class FeuilleDeMatchDTO {

    private Long feuilleId; // ✅ ID de la feuille de match
    private Long rid; // ✅ ID de la rencontre
    private Long jid;

    private String nom;
    private String poste;
    private int buts = 0;
    private int passes = 0;
    private double cote = 5.0;
    private double minutesJouees = 0; // ✅ Stocke `double` basé sur `TimePlay`
    @Getter
    private boolean titulaire; // ✅ Si le joueur est titulaire
    @Getter
    private boolean ajoue; // ✅ Si le joueur est entré en jeu
    private int butArreter = 0; // ✅ Buts arrêtés (pour les gardiens)
    private int butEncaisser = 0; // ✅ Buts encaissés (pour les gardiens)
    private List<Long> passeursId; // ✅ IDs des passeurs
    private List<String> nomsPasseurs; // ✅ Noms des passeurs (pour affichage)







    public FeuilleDeMatchDTO(Long feuilleId, Long rid, Long jid, String nom, String poste,
                             int buts, int passes, double cote, double minutesJouees,
                             boolean ajoue, boolean titulaire, int butArreter, int butEncaisser,
                             List<Long> passeursId, List<String> nomsPasseurs
                             ) {
        this.jid = jid;
        this.rid = rid;
        this.jid = jid;
        this.nom = nom;
        this.poste = (poste != null && !poste.isEmpty()) ? poste : "INCONNU";
        this.buts = buts;
        this.passes = passes;
        this.cote = cote;
        this.minutesJouees = minutesJouees;
        this.ajoue = ajoue;
        this.titulaire = titulaire;
        this.butArreter = butArreter;
        this.butEncaisser = butEncaisser;

        this.passeursId = (passeursId != null) ? new ArrayList<>(passeursId) : new ArrayList<>();
        this.nomsPasseurs = (nomsPasseurs != null) ? new ArrayList<>(nomsPasseurs) : new ArrayList<>();
        this.jid = jid;
    }






    /**
     * ✅ Convertit `double` en `TimePlay`
     */
    public TimePlay getMinutesJoueesEnum() {
        return TimePlay.fromPercentage(this.minutesJouees);
    }

    /**
     * ✅ Définit les minutes jouées en `TimePlay`
     */
    public void setMinutesJoueesEnum(TimePlay timePlay) {
        this.minutesJouees = timePlay.getPercentage();
    }

    

    /**
     * ✅ Convertit une chaîne d'IDs en liste d'IDs
     */
    public void setPasseursIdFromString(String passeursIds) {
        if (passeursIds != null && !passeursIds.isEmpty()) {
            this.passeursId = Arrays.stream(passeursIds.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } else {
            this.passeursId = new ArrayList<>();
        }
    }

    /**
     * ✅ Convertit une chaîne de noms en liste de noms
     */
    public void setNomsPasseursFromString(String nomsPasseurs) {
        if (nomsPasseurs != null && !nomsPasseurs.isEmpty()) {
            this.nomsPasseurs = Arrays.asList(nomsPasseurs.split(","));
        } else {
            this.nomsPasseurs = new ArrayList<>();
        }
    }


    public static FeuilleDeMatchDTO fromProjection(FeuilleDeMatchProjection projection) {
        return new FeuilleDeMatchDTO(
                projection.getId(),
                projection.getRid(),
                projection.getJid(),
                projection.getNom(),
                projection.getPoste(),
                projection.getButs(),
                projection.getPasses(),
                projection.getCote(),
                projection.getMinutesJouees(),
                projection.isAjoue(),
                projection.isTitulaire(),
                projection.getButArreter(),
                projection.getButEncaisser(),
                projection.getPasseursIds() != null && !projection.getPasseursIds().isEmpty()
                        ? new ArrayList<Long>(Arrays.stream(projection.getPasseursIds().split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList())) // ✅ Forcer ArrayList<Long>
                        : new ArrayList<Long>(), // ✅ Ajoute explicitement `<Long>`
                projection.getNomsPasseurs() != null && !projection.getNomsPasseurs().isEmpty()
                        ? new ArrayList<String>(Arrays.asList(projection.getNomsPasseurs().split(","))) // ✅ Forcer ArrayList<String>
                        : new ArrayList<String>() // ✅ Ajoute explicitement `<String>`
        );
    }
}

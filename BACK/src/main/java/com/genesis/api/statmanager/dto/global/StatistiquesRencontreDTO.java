package com.genesis.api.statmanager.dto.global;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.model.FeuilleDeMatch;
import com.genesis.api.statmanager.model.Joueur;
import com.genesis.api.statmanager.model.enumeration.Poste;
import com.genesis.api.statmanager.projection.FeuilleDeMatchProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesRencontreDTO extends StatistiquesDTO {
    private Long rencontreId;
    private double minutesJouees;
    private double cote;
    private List<Long> idPasseurs;  // Liste des IDs des passeurs
    private List<String> nomPasseurs;  // Liste des noms des passeurs

    public double getButsParMinute() {
        return (minutesJouees > 0) ? (double) buts / minutesJouees : 0;
    }

    public double getPassesParMinute() {
        return (minutesJouees > 0) ? (double) passes / minutesJouees : 0;
    }

    public double getPointsParMinute() {
        return (minutesJouees > 0) ? (double) points / minutesJouees : 0;
    }

    // Méthode pour agréger les statistiques de la rencontre
    public static StatistiquesRencontreDTO fromFeuilleDeMatch(FeuilleDeMatchDTO feuilleDeMatch) {
        StatistiquesRencontreDTO stats = new StatistiquesRencontreDTO();
        stats.setRencontreId(feuilleDeMatch.getRid());
        stats.setMinutesJouees(feuilleDeMatch.getMinutesJouees());
        stats.setButs(feuilleDeMatch.getButs());
        stats.setPasses(feuilleDeMatch.getPasses());
        stats.setCote(feuilleDeMatch.getMoyenneCote());

        // Assigner les passeurs
        stats.setIdPasseurs(feuilleDeMatch.getPasseursId());
        stats.setNomPasseurs(feuilleDeMatch.getNomsPasseurs());

        // Calcul des points en fonction du barème
        int points = (feuilleDeMatch.getButs() * 4) +
                (feuilleDeMatch.getPasses() * 3) +
                ((int) (feuilleDeMatch.getMinutesJouees() * 0.02)) +
                ((int) (feuilleDeMatch.getMoyenneCote() * 2));

        // Malus pour buts encaissés (si le joueur est défenseur ou gardien)
        if ("Défenseur".equals(feuilleDeMatch.getPoste()) || "Gardien".equals(feuilleDeMatch.getPoste())) {
            points -= feuilleDeMatch.getButEncaisser();

            // Bonus Clean Sheet (si aucun but encaissé)
            if (feuilleDeMatch.getButEncaisser() == 0) {
                points += 5;
            }
        }

        stats.setPoints(points);
        return stats;
    }



    public static StatistiquesRencontreDTO fromFeuilleDeMatchEntity(FeuilleDeMatch feuilleDeMatch, Poste poste) {
        StatistiquesRencontreDTO stats = new StatistiquesRencontreDTO();
        stats.setRencontreId(feuilleDeMatch.getRencontre().getRid());
        stats.setJoueurId(feuilleDeMatch.getJoueurId());
        stats.setMinutesJouees(feuilleDeMatch.getMinutesJouees());
        stats.setButs(feuilleDeMatch.getButs());
        stats.setPasses(feuilleDeMatch.getPasses());
        stats.setCote(feuilleDeMatch.getMoyenneCote());

        // Calcul des points
        int points = (feuilleDeMatch.getButs() * 4) +
                (feuilleDeMatch.getPasses() * 3) +
                ((int) (feuilleDeMatch.getMinutesJouees() * 0.02)) +
                ((int) (feuilleDeMatch.getMoyenneCote() * 2));

        // ✅ Malus et bonus pour défenseurs/gardiens (passé en paramètre)
        if (poste.isDefensif() || poste.isGardien()) {
            points -= feuilleDeMatch.getButEncaisser();
            if (feuilleDeMatch.getButEncaisser() == 0) {
                points += 5;
            }
        }

        stats.setPoints(points);
        return stats;
    }



    public static StatistiquesRencontreDTO fromFeuilleDeMatchProjection(FeuilleDeMatchProjection feuilleProjection) {
        StatistiquesRencontreDTO stats = new StatistiquesRencontreDTO();

        // Remplir les propriétés de stats avec celles de la projection
        stats.setRencontreId(feuilleProjection.getRid());
        stats.setMinutesJouees(feuilleProjection.getMinutesJouees());
        stats.setButs(feuilleProjection.getButs());
        stats.setPasses(feuilleProjection.getPasses());
        stats.setCote(feuilleProjection.getMoyenneCote());

        // Assigner les passeurs, conversion des chaînes en List<Long> et List<String>
        stats.setIdPasseurs(parsePasseurs(feuilleProjection.getPasseursIds()));
        stats.setNomPasseurs(parseNomsPasseurs(feuilleProjection.getNomsPasseurs()));

        // Calcul des points
        stats.setPoints(feuilleProjection.getButs() * 3 + feuilleProjection.getPasses());

        return stats;
    }


    // Constructeur avec tous les paramètres nécessaires
    public StatistiquesRencontreDTO(Long joueurId, String nom, String typeJoueur, String poste,
                                    Integer buts, double butsParMinute, Integer passes, double passesParMinute,
                                    double moyenneButsPasses, Double cote, int points, int pointsParMinute,
                                    Double totalMinutesJouees, Long rencontreId, List<Long> idPasseurs,
                                    List<String> nomPasseurs) {
        this.joueurId = joueurId;
        this.nom = nom;
        this.typeJoueur = typeJoueur;
        this.poste = poste;
        this.buts = buts;
        this.passes = passes;
        this.cote = cote;
        this.points = points;
        this.totalMinutesJouees = totalMinutesJouees;
        this.rencontreId = rencontreId;
        this.idPasseurs = idPasseurs;
        this.nomPasseurs = nomPasseurs;
    }



    public static Long determinerHommeDuMatch(List<StatistiquesRencontreDTO> statsRencontres) {
        if (statsRencontres.isEmpty()) {
            return null;
        }

        // 1️⃣ Trouver le joueur avec le plus de points
        return statsRencontres.stream()
                .max(Comparator.comparingInt(StatistiquesRencontreDTO::getPoints))
                .map(StatistiquesRencontreDTO::getJoueurId)
                .orElse(null);
    }


    private static List<Long> parsePasseurs(String passeursIds) {
        if (passeursIds != null && !passeursIds.isEmpty()) {
            return Arrays.stream(passeursIds.split(","))
                    .map(Long::parseLong) // Convertit chaque ID en Long
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }


    private static List<String> parseNomsPasseurs(String nomsPasseurs) {
        if (nomsPasseurs != null && !nomsPasseurs.isEmpty()) {
            return Arrays.asList(nomsPasseurs.split(",")); // Convertit directement en liste de String
        } else {
            return new ArrayList<>();
        }
    }


    public StatistiquesRencontreDTO(Long joueurId, String nom, String poste,
                                    Integer buts, Integer passes, Double cote, double minutesJouees,
                                    Long rencontreId, List<Long> idPasseurs, List<String> nomPasseurs) {
        this.joueurId = joueurId;
        this.nom = nom;
        this.poste = poste;
        this.buts = buts;
        this.passes = passes;
        this.cote = cote;
        this.minutesJouees = minutesJouees;
        this.rencontreId = rencontreId;
        this.idPasseurs = idPasseurs;
        this.nomPasseurs = nomPasseurs;
    }


    public StatistiquesRencontreDTO(Long joueurId, String nom, String typeJoueur, Poste poste,
                                    int buts, int passes, double moyenneCote, double minutesJouees, Long rencontreId) {
        this.joueurId = joueurId;
        this.nom = (nom != null) ? nom : "Inconnu";
        this.typeJoueur = (typeJoueur != null) ? typeJoueur : "Non défini";
        this.poste = String.valueOf(poste); // ✅ Gère le null pour Enum
        this.buts = buts;
        this.passes = passes;
        this.cote = moyenneCote;
        this.minutesJouees = minutesJouees;
        this.rencontreId = rencontreId;
    }









}

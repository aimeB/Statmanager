package com.genesis.api.statmanager.dto.global;

import com.genesis.api.statmanager.model.enumeration.PointJoueur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesChampionnatDTO extends StatistiquesDTO {
    private Long championnatId;
    private int butsChamp = 0;
    private int passesChamp = 0;
    private double coteChamp = 5.0;
    private double minutesJoueesChamp = 0.0;
    // Calcul des points
    private int pointsChamp;
    // Constructeur avec les paramètres nécessaires
    public StatistiquesChampionnatDTO(Long championnatId, Long joueurId, String nom, String poste,
                                      int butsChamp, int passesChamp, double coteChamp, double minutesJoueesChamp, int pointsChamp) {
        this.championnatId = championnatId;
        this.joueurId = joueurId;
        this.nom = nom;
        this.poste = poste;
        this.butsChamp = butsChamp;
        this.passesChamp = passesChamp;
        this.coteChamp = coteChamp;
        this.minutesJoueesChamp = minutesJoueesChamp;
        this.pointsChamp = pointsChamp;  // Calcul des points
    }

    // Méthode pour calculer les statistiques d'un joueur dans un championnat spécifique
    public static StatistiquesChampionnatDTO fromChampionnat(List<StatistiquesRencontreDTO> statsRencontres, Long championnatId) {
        StatistiquesChampionnatDTO statsChamp = new StatistiquesChampionnatDTO();
        statsChamp.championnatId = championnatId;

        int totalButs = 0;
        int totalPasses = 0;
        double totalMinutes = 0;
        double totalCote = 0;
        int totalPoints = 0;
        int matchCount = statsRencontres.size();

        if (matchCount == 0) {
            return statsChamp;  // Retourne un DTO vide si aucune rencontre
        }

        // Agrégation des statistiques
        for (StatistiquesRencontreDTO stats : statsRencontres) {
            totalButs += stats.getButs();
            totalPasses += stats.getPasses();
            totalMinutes += stats.getMinutesJouees();
            totalCote += stats.getCote();
            totalPoints += stats.getPoints();
        }

        statsChamp.butsChamp = totalButs;
        statsChamp.passesChamp = totalPasses;
        statsChamp.minutesJoueesChamp = totalMinutes;
        statsChamp.coteChamp = (totalMinutes > 0) ? totalCote / totalMinutes : 5.0;

        statsChamp.pointsChamp = totalPoints; // ✅ Ajout de l'agrégation des points

        return statsChamp;
    }




    public static void attribuerPointsFinalChampionnat(List<StatistiquesChampionnatDTO> statsChampionnat) {
        // Tri des joueurs par total de points décroissant
        statsChampionnat.sort((a, b) -> Integer.compare(b.getPointsChamp(), a.getPointsChamp()));

        // Attribution des points selon le classement avec PointJoueur
        for (int i = 0; i < statsChampionnat.size(); i++) {
            StatistiquesChampionnatDTO joueur = statsChampionnat.get(i);
            PointJoueur pointsAttribues;

            if (i == 0) {
                pointsAttribues = PointJoueur.FIRST;
            } else if (i == 1) {
                pointsAttribues = PointJoueur.SECOND;
            } else if (i == 2) {
                pointsAttribues = PointJoueur.THIRD;
            } else if (i < 4) {
                pointsAttribues = PointJoueur.MOTION;
            } else {
                pointsAttribues = PointJoueur.NULL;
            }

            // Mise à jour des points du joueur en base
            joueur.setPointsChamp(joueur.getPointsChamp() + pointsAttribues.getValue());
        }
    }

}

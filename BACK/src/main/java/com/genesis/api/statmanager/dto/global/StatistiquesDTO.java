package com.genesis.api.statmanager.dto.global;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class StatistiquesDTO {
    protected Long joueurId;
    protected String nom;
    protected String typeJoueur;
    protected String poste;
    protected int buts = 0;
    protected int passes = 0;
    protected double cote = 5.0;
    protected int points = 0;
    protected double totalMinutesJouees = 0.0;

    public double getButsParMinute() {
        return (totalMinutesJouees > 0) ? (double) buts / totalMinutesJouees : 0;
    }

    public double getPassesParMinute() {
        return (totalMinutesJouees > 0) ? (double) passes / totalMinutesJouees : 0;
    }

    public double getPointsParMinute() {
        return (totalMinutesJouees > 0) ? (double) points / totalMinutesJouees : 0;
    }

    public double getMoyenneButsPasses() {
        return (getButsParMinute() + getPassesParMinute()) / 2;
    }

    // Méthode de calcul des statistiques globales d'un joueur
    public static StatistiquesDTO fromRencontres(List<StatistiquesRencontreDTO> statsRencontres) {
        if (statsRencontres == null || statsRencontres.isEmpty()) {
            return new StatistiquesDTO();
        }

        StatistiquesDTO totalStats = new StatistiquesDTO();
        totalStats.joueurId = statsRencontres.get(0).getJoueurId();
        totalStats.nom = statsRencontres.get(0).getNom();
        totalStats.typeJoueur = statsRencontres.get(0).getTypeJoueur();
        totalStats.poste = statsRencontres.get(0).getPoste();

        int totalButs = 0;
        int totalPasses = 0;
        double totalMinutes = 0;
        double totalCote = 0;
        int totalPoints = 0;
        int matchCount = statsRencontres.size();

        for (StatistiquesRencontreDTO stats : statsRencontres) {
            totalButs += stats.getButs();
            totalPasses += stats.getPasses();
            totalMinutes += stats.getMinutesJouees();
            totalCote += stats.getCote();
            totalPoints += stats.getPoints(); // ✅ Prendre les points calculés dans `StatistiquesRencontreDTO`
        }

        totalStats.buts = totalButs;
        totalStats.passes = totalPasses;
        totalStats.totalMinutesJouees = totalMinutes;
        totalStats.points = totalPoints; // ✅ Corrigé, on ne fait plus `totalButs * 3 + totalPasses`

        // Calcul de la moyenne de la cote
        totalStats.cote = ( totalStats.totalMinutesJouees > 0) ? totalCote /  totalStats.totalMinutesJouees : 5.0;


        return totalStats;
    }




    public StatistiquesDTO(Long joueurId, String nom, String typeJoueur, String poste,
                           int buts, int passes, double cote, int points, double totalMinutesJouees) {
        this.joueurId = joueurId;
        this.nom = nom;
        this.typeJoueur = typeJoueur;
        this.poste = poste;
        this.buts = buts;
        this.passes = passes;
        this.cote = cote;
        this.points = points;
        this.totalMinutesJouees = totalMinutesJouees;
    }

}

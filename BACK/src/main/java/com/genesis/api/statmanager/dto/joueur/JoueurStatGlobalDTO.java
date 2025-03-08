package com.genesis.api.statmanager.dto.joueur;

import com.genesis.api.statmanager.dto.global.StatistiquesRencontreDTO;
import com.genesis.api.statmanager.model.enumeration.Division;
import com.genesis.api.statmanager.projection.JoueurProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoueurStatGlobalDTO {
    private Long joueurId;
    private String nom;
    private String typeJoueur;
    private String poste;
    private int totalButsGlobal;
    private int totalPassesGlobal;
    private double totalMinutesJoueesGlobal;
    private double moyenneCoteGlobal;

    private double scoreOffensifGlobal;
    private double scoreRegulariteGlobal;
    private double scoreContributionGlobal;
    private double scoreEnduranceGlobal;

    private Map<String, PerformanceParDivision> performancesParDivision;
    private List<StatistiquesRencontreDTO> derniersMatchs;

    public JoueurStatGlobalDTO(Long joueurId, String nom, String typeJoueur, String poste,
                               int totalButsGlobal, int totalPassesGlobal, double totalMinutesJoueesGlobal,
                               double moyenneCoteGlobal, Map<String, PerformanceParDivision> performancesParDivision,
                               List<StatistiquesRencontreDTO> derniersMatchs) {
        this.joueurId = joueurId;
        this.nom = nom;
        this.typeJoueur = typeJoueur;
        this.poste = poste;
        this.totalButsGlobal = totalButsGlobal;
        this.totalPassesGlobal = totalPassesGlobal;
        this.totalMinutesJoueesGlobal = totalMinutesJoueesGlobal;
        this.moyenneCoteGlobal = moyenneCoteGlobal;
        this.performancesParDivision = performancesParDivision;
        this.derniersMatchs = derniersMatchs;
        this.calculerScoresGlobal(); // Calcul des scores après initialisation
    }

    public void calculerScoresGlobal() {
        if (totalMinutesJoueesGlobal > 0) {  // ✅ Vérifie si le joueur a joué avant de calculer
            this.scoreOffensifGlobal = calculerScoreOffensifGlobal();
            this.scoreRegulariteGlobal = calculerScoreRegulariteGlobal();
            this.scoreContributionGlobal = calculerScoreContributionGlobal();
            this.scoreEnduranceGlobal = calculerScoreEnduranceGlobal();
        } else {
            this.scoreOffensifGlobal = 0;
            this.scoreRegulariteGlobal = 0;
            this.scoreContributionGlobal = 0;
            this.scoreEnduranceGlobal = 0;
        }
    }


    public double calculerScoreOffensifGlobal() {
        return (totalButsGlobal * 5) + (totalPassesGlobal * 3) + (moyenneCoteGlobal * 2);
    }

    public double calculerScoreRegulariteGlobal() {
        return ((double) totalButsGlobal / (totalMinutesJoueesGlobal + 1) * 4) +
                ((double) totalPassesGlobal / (totalMinutesJoueesGlobal + 1) * 4) +
                (moyenneCoteGlobal * 2);
    }

    public double calculerScoreContributionGlobal() {
        return (totalPassesGlobal * 3) + (totalMinutesJoueesGlobal * 2) + (moyenneCoteGlobal * 3);
    }

    public double calculerScoreEnduranceGlobal() {
        return (totalMinutesJoueesGlobal * 5) + (moyenneCoteGlobal * 2);
    }

    public static int comparerParCritere(JoueurStatGlobalDTO j1, JoueurStatGlobalDTO j2, String critere) {
        return switch (critere) {
            case "offensif" -> Double.compare(j2.getScoreOffensifGlobal(), j1.getScoreOffensifGlobal());
            case "regularite" -> Double.compare(j2.getScoreRegulariteGlobal(), j1.getScoreRegulariteGlobal());
            case "contribution" -> Double.compare(j2.getScoreContributionGlobal(), j1.getScoreContributionGlobal());
            case "endurance" -> Double.compare(j2.getScoreEnduranceGlobal(), j1.getScoreEnduranceGlobal());
            default -> 0;
        };
    }

    public static JoueurStatGlobalDTO fromProjection(JoueurProjection projection) {
        return new JoueurStatGlobalDTO(
                projection.getJid(),
                projection.getNom(),
                projection.getTypeJoueur().getSimpleName() , // ✅ Convertir `Class<?>` en `String`
                projection.getPoste(),
                projection.getTotalButs(),
                projection.getTotalPasses(),
                projection.getTotalMinutesJouees(),
                projection.getTotalMoyenneCote(),
                null, // performancesParDivision (à récupérer séparément si nécessaire)
                null  // derniersMatchs (à récupérer séparément si nécessaire)
        );
    }


}

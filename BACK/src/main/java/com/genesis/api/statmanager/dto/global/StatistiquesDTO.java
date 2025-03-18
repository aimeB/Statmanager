package com.genesis.api.statmanager.dto.global;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
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

    // 📌 Ajout des stats spécifiques aux gardiens
    protected int butArreter = 0;
    protected int butEncaisser = 0;
    protected int cleanSheet = 0; // ✅ Nombre total de clean sheets

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

    /**
     * 📌 Génère les statistiques globales d'un joueur à partir de ses rencontres
     */
    public static StatistiquesDTO fromRencontres(List<StatistiquesRencontreDTO> statsRencontres) {
        if (statsRencontres == null || statsRencontres.isEmpty()) {
            log.warn("⚠️ Aucune rencontre trouvée pour générer les statistiques globales !");
            return new StatistiquesDTO();
        }

        log.info("📌 Début du calcul des statistiques globales du joueur - Nombre de matchs : {}", statsRencontres.size());

        StatistiquesDTO totalStats = new StatistiquesDTO();
        totalStats.joueurId = statsRencontres.get(0).getJoueurId();
        totalStats.nom = statsRencontres.get(0).getNom();
        totalStats.typeJoueur = statsRencontres.get(0).getTypeJoueur();
        totalStats.poste = statsRencontres.get(0).getPoste();

        int totalButs = 0;
        int totalPasses = 0;
        double totalMinutes = 0;
        double totalCotePonderee = 0;
        int totalButsArretes = 0;
        int totalButsEncaisses = 0;
        int totalCleanSheets = 0;

        for (StatistiquesRencontreDTO stats : statsRencontres) {
            log.info("📊 Match traité - Joueur={} | Buts={} | Passes={} | Minutes={} | Cote={} | Buts Arrêtés={} | Buts Encaissés={}",
                    stats.getJoueurId(), stats.getButs(), stats.getPasses(), stats.getMinutesJouees(),
                    stats.getCote(), stats.getButArreter(), stats.getButEncaisser());

            totalButs += stats.getButs();
            totalPasses += stats.getPasses();
            totalMinutes += stats.getMinutesJouees();
            totalButsArretes += stats.getButArreter();
            totalButsEncaisses += stats.getButEncaisser();

            // ✅ Calcul de la cote pondérée (déplacé ici)
            totalCotePonderee += stats.getCote() * stats.getMinutesJouees();

            if ("GB".equalsIgnoreCase(stats.getPoste()) && stats.getButEncaisser() == 0) {
                totalCleanSheets++;
            }
        }

        // ✅ Attribution des valeurs calculées
        totalStats.buts = totalButs;
        totalStats.passes = totalPasses;
        totalStats.totalMinutesJouees = totalMinutes;
        totalStats.butArreter = totalButsArretes;
        totalStats.butEncaisser = totalButsEncaisses;
        totalStats.cleanSheet = totalCleanSheets;

        // ✅ Calcul correct de la moyenne pondérée
        totalStats.cote = (totalMinutes > 0) ? totalCotePonderee / totalMinutes : 5.0;

        log.info("✅ Statistiques finales calculées - Joueur ID={} | Total Buts={} | Total Passes={} | Total Minutes={} | Moyenne Cote={} | Buts Arrêtés={} | Buts Encaissés={} | Clean Sheets={}",
                totalStats.joueurId, totalStats.buts, totalStats.passes, totalStats.totalMinutesJouees,
                totalStats.cote, totalStats.butArreter, totalStats.butEncaisser, totalStats.cleanSheet);

        return totalStats;
    }


}

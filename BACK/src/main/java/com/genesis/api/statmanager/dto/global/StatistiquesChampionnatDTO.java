package com.genesis.api.statmanager.dto.global;

import com.genesis.api.statmanager.model.enumeration.PointJoueur;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesChampionnatDTO extends StatistiquesRencontreDTO {
    private Long championnatId;
    private int butsChamp = 0;
    private int passesChamp = 0;
    private double coteChamp = 5.0;
    private double minutesJoueesChamp = 0.0;
    private int pointsChamp;

    // 📌 Ajout des statistiques spécifiques aux gardiens
    private int butArreterChamp = 0;  // 🥅 Nombre d'arrêts réalisés par un gardien
    private int butEncaisserChamp = 0; // 🚨 Nombre de buts encaissés
    private int cleanSheet = 0; // 🏆 Nombre de matchs sans encaisser de but (clean sheets)







    // Constructeur avec les paramètres nécessaires
    // ✅ Constructeur modifié pour inclure les nouvelles stats
    public StatistiquesChampionnatDTO(Long championnatId, Long joueurId, String nom, String poste,
                                      int butsChamp, int passesChamp, double coteChamp, double minutesJoueesChamp,
                                      int pointsChamp, int butArreterChamp, int butEncaisserChamp, int cleanSheet) {
        this.championnatId = championnatId;
        this.joueurId = joueurId;
        this.nom = nom;
        this.poste = poste;
        this.butsChamp = butsChamp;
        this.passesChamp = passesChamp;
        this.coteChamp = coteChamp;
        this.minutesJoueesChamp = minutesJoueesChamp;
        this.pointsChamp = pointsChamp;
        this.butArreterChamp = butArreterChamp;
        this.butEncaisserChamp = butEncaisserChamp;
        this.cleanSheet = cleanSheet;
    }












    // Méthode pour calculer les statistiques d'un joueur dans un championnat spécifique
    public static StatistiquesChampionnatDTO fromChampionnat(List<StatistiquesRencontreDTO> statsRencontres, Long championnatId) {
        log.info("📌 Début de l’agrégation des statistiques du championnat ID={}", championnatId);

        StatistiquesChampionnatDTO statsChamp = new StatistiquesChampionnatDTO();
        statsChamp.championnatId = championnatId;

        if (statsRencontres.isEmpty()) {
            log.warn("⚠️ Aucun match trouvé pour le championnat ID={}", championnatId);
            return statsChamp;
        }

        int totalButs = 0;
        int totalPasses = 0;
        double totalMinutes = 0;
        double totalCotePonderee = 0;
        int totalPoints = 0;
        int totalArrets = 0;
        int totalButsEncaisses = 0;
        int totalCleanSheets = 0;

        for (StatistiquesRencontreDTO stats : statsRencontres) {
            log.info("📊 Joueur={} | Poste={} | Buts={} | Passes={} | Minutes={} | Cote={} | Points={} | Arrêts={} | Buts Encaissés={}",
                    stats.getJoueurId(), stats.getPoste(), stats.getButs(), stats.getPasses(),
                    stats.getMinutesJouees(), stats.getCote(), stats.getPoints(),
                    stats.getButArreter(), stats.getButEncaisser());


            totalButs += stats.getButs();
            totalPasses += stats.getPasses();
            totalMinutes += stats.getMinutesJouees();
            totalPoints += stats.getPoints();
            totalArrets += stats.getButArreter();
            totalButsEncaisses += stats.getButEncaisser();
            // ✅ Stockage du poste depuis le premier élément (car tous les joueurs de ce championnat ont le même poste)
            statsChamp.setPoste(stats.getPoste());
            // ✅ Cote pondérée en fonction du temps joué
            totalCotePonderee += stats.getCote() * stats.getMinutesJouees();

            // ✅ Un clean sheet est ajouté si un gardien a joué et n’a encaissé aucun but
            if ("GB".equalsIgnoreCase(stats.getPoste()) && stats.getButEncaisser() == 0) {
                totalCleanSheets++;
            }
        }




        statsChamp.butsChamp = totalButs;
        statsChamp.passesChamp = totalPasses;
        statsChamp.minutesJoueesChamp = totalMinutes;

        // ✅ Cote moyenne pondérée calculée correctement
        statsChamp.coteChamp = (totalMinutes > 0) ? totalCotePonderee / totalMinutes : 5.0;

        statsChamp.pointsChamp = totalPoints;
        statsChamp.butArreterChamp = totalArrets;
        statsChamp.butEncaisserChamp = totalButsEncaisses;
        statsChamp.cleanSheet = totalCleanSheets;

        log.info("✅ Statistiques calculées pour championnat ID={} | Poste={} | Total Buts={} | Total Passes={} | Total Minutes={} | Cote Pondérée={} | Total Points={} | Arrêts={} | Buts Encaissés={} | Clean Sheets={}",
                championnatId, statsRencontres.get(0).getPoste(), totalButs, totalPasses, totalMinutes, statsChamp.coteChamp, totalPoints, totalArrets, totalButsEncaisses, totalCleanSheets);



        return statsChamp;
    }







    public static void attribuerPointsFinalChampionnat(List<StatistiquesChampionnatDTO> statsChampionnat) {
        log.info("📌 Début de l’attribution des points finaux pour le championnat.");

        statsChampionnat.sort((a, b) -> Integer.compare(b.getPointsChamp(), a.getPointsChamp()));

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

            log.info("🏆 Attribution des points - Joueur ID={} | Classement={} | Points Actuels={} | Points Attribués={}",
                    joueur.getJoueurId(), i + 1, joueur.getPointsChamp(), pointsAttribues.getValue());

            joueur.setPointsChamp(joueur.getPointsChamp() + pointsAttribues.getValue());
        }

        log.info("✅ Fin de l’attribution des points finaux.");
    }





}

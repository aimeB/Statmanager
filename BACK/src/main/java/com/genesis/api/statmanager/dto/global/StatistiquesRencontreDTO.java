package com.genesis.api.statmanager.dto.global;

import com.genesis.api.statmanager.dto.FeuilleDeMatchDTO;
import com.genesis.api.statmanager.dto.rencontre.ClotureRencontreDTO;
import com.genesis.api.statmanager.model.FeuilleDeMatch;
import com.genesis.api.statmanager.model.Joueur;
import com.genesis.api.statmanager.model.enumeration.Poste;
import com.genesis.api.statmanager.projection.FeuilleDeMatchProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;



import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesRencontreDTO extends StatistiquesDTO {
    private Long rencontreId;
    private double minutesJouees;
    private List<Long> idPasseurs;
    private List<String> nomPasseurs;


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
        log.info("📌 Conversion FeuilleDeMatchDTO → StatistiquesRencontreDTO | Joueur={} | Buts={} | Passes={} | Cote avant correction={}",
                feuilleDeMatch.getJid(), feuilleDeMatch.getButs(), feuilleDeMatch.getPasses(), feuilleDeMatch.getCote());

        StatistiquesRencontreDTO stats = new StatistiquesRencontreDTO();
        stats.setRencontreId(feuilleDeMatch.getRid());
        stats.setJoueurId(feuilleDeMatch.getJid());
        stats.setMinutesJouees(feuilleDeMatch.getMinutesJouees());
        stats.setButs(feuilleDeMatch.getButs());
        stats.setPasses(feuilleDeMatch.getPasses());

        stats.setIdPasseurs(feuilleDeMatch.getPasseursId());
        stats.setNomPasseurs(feuilleDeMatch.getNomsPasseurs());

        // ✅ Assurer que le poste est bien stocké
        String posteFinal = feuilleDeMatch.getPoste() != null ? feuilleDeMatch.getPoste() : "INCONNU";
        stats.setPoste(posteFinal);
        stats.setCote(feuilleDeMatch.getCote());  // ✅ Mettre à jour la cote dans StatistiquesRencontreDTO

        log.info("📌 Cote mise à jour pour Joueur={} | Nouvelle Cote={}", feuilleDeMatch.getJid(), feuilleDeMatch.getCote());

        // ✅ Calcul des points
        int points = (feuilleDeMatch.getButs() * 4) +
                (feuilleDeMatch.getPasses() * 3) +
                ((int) (feuilleDeMatch.getMinutesJouees() * 0.02)) +
                ((int) (feuilleDeMatch.getCote() * 2)); // 🔥 Utiliser la nouvelle cote pour les points

        // ✅ Ajustement pour défenseurs et gardiens
        if ("Défenseur".equalsIgnoreCase(stats.getPoste()) || "Gardien".equalsIgnoreCase(stats.getPoste())) {
            points -= feuilleDeMatch.getButEncaisser();
            if (feuilleDeMatch.getButEncaisser() == 0) {
                points += 5;
            }
        }

        stats.setPoints(points);

        log.info("📌 Stats après attribution des points (fromFeuilleDeMatch)  | Joueur={} | Points={} | Buts={} | Passes={} | Minutes Jouées={} | Cote Finale={}  | Poste={}",
                feuilleDeMatch.getJid(), stats.getPoints(), stats.getButs(), stats.getPasses(), stats.getMinutesJouees(), stats.getCote(), stats.getPoste());

        return stats;
    }






    public static StatistiquesRencontreDTO fromFeuilleDeMatchEntity(FeuilleDeMatch feuilleDeMatch, Poste poste) {
        StatistiquesRencontreDTO stats = new StatistiquesRencontreDTO();
        stats.setRencontreId(feuilleDeMatch.getRencontre().getRid());
        stats.setJoueurId(feuilleDeMatch.getJid());
        stats.setMinutesJouees(feuilleDeMatch.getMinutesJouees());
        stats.setButs(feuilleDeMatch.getButs());
        stats.setPasses(feuilleDeMatch.getPasses());
        stats.setCote(feuilleDeMatch.getCote());

        // 🚨 Vérification stricte pour éviter `null`
        if (poste == null) {
            throw new IllegalStateException("❌ ERREUR CRITIQUE : Le poste est null pour Joueur ID=" + feuilleDeMatch.getJid());
        }
        stats.setPoste(poste.name());

        // ✅ Calcul des points
        int points = (feuilleDeMatch.getButs() * 4) +
                (feuilleDeMatch.getPasses() * 3) +
                ((int) (feuilleDeMatch.getMinutesJouees() * 0.02)) +
                ((int) (feuilleDeMatch.getCote() * 2));

        // ✅ Malus et bonus pour défenseurs/gardiens
        if (poste.isDefensif() || poste.isGardien()) {
            points -= feuilleDeMatch.getButEncaisser();
            if (feuilleDeMatch.getButEncaisser() == 0) {
                points += 5;
            }
        }

        stats.setPoints(points);
        return stats;
    }






    // 📌 Mise à jour pour inclure les stats spécifiques aux gardiens
    public static StatistiquesRencontreDTO fromFeuilleDeMatchProjection(FeuilleDeMatchProjection feuilleProjection) {
        StatistiquesRencontreDTO stats = new StatistiquesRencontreDTO();

        stats.setJoueurId(feuilleProjection.getJid());
        stats.setRencontreId(feuilleProjection.getRid());
        stats.setMinutesJouees(feuilleProjection.getMinutesJouees());
        stats.setButs(feuilleProjection.getButs());
        stats.setPasses(feuilleProjection.getPasses());
        stats.setCote(feuilleProjection.getCote());
        stats.setIdPasseurs(parsePasseurs(feuilleProjection.getPasseursIds()));
        stats.setNomPasseurs(parseNomsPasseurs(feuilleProjection.getNomsPasseurs()));
        stats.setButEncaisser(feuilleProjection.getButEncaisser());
        stats.setButArreter(feuilleProjection.getButArreter());

        // 🚨 Vérification stricte pour éviter `null`
        String posteFinal = feuilleProjection.getPoste();
        if (posteFinal == null || posteFinal.isEmpty()) {
            throw new IllegalStateException("❌ ERREUR CRITIQUE : Le poste est null pour Joueur ID=" + feuilleProjection.getJid());
        }
        stats.setPoste(posteFinal);

        int points = feuilleProjection.getButs() * 3 + feuilleProjection.getPasses();

        if (stats.getPoste().equalsIgnoreCase("Gardien")) {
            points += feuilleProjection.getButArreter() * 2;
            points -= feuilleProjection.getButEncaisser();
        }

        stats.setPoints(points);

        log.info("📌 Stats après conversion | Joueur={} | Poste={} | Points={} | Buts Arrêtés={} | Buts Encaissés={}",
                stats.getJoueurId(), stats.getPoste(), stats.getPoints(), stats.getButArreter(), stats.getButEncaisser());

        return stats;
    }





    public StatistiquesRencontreDTO(Long joueurId, String nom, String typeJoueur, String poste,
                                    Integer buts, double butsParMinute, Integer passes, double passesParMinute,
                                    double moyenneButsPasses, Double cote, int points, int pointsParMinute,
                                    double totalMinutesJouees, int butArreter, int butEncaisser,
                                    Long rencontreId, List<Long> idPasseurs, List<String> nomPasseurs) {
        this.joueurId = joueurId;
        this.nom = nom;
        this.typeJoueur = typeJoueur;
        this.poste = poste;
        this.buts = buts != null ? buts : 0;  // ✅ Correction pour éviter les `null`
        this.passes = passes != null ? passes : 0;
        this.cote = cote != null ? cote : 5.0; // ✅ Valeur par défaut
        this.points = points;
        this.totalMinutesJouees = totalMinutesJouees;
        this.butArreter = butArreter;  // ✅ Ajout des buts arrêtés
        this.butEncaisser = butEncaisser;  // ✅ Ajout des buts encaissés
        this.rencontreId = rencontreId;
        this.idPasseurs = idPasseurs != null ? idPasseurs : new ArrayList<>();
        this.nomPasseurs = nomPasseurs != null ? nomPasseurs : new ArrayList<>();
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
        log.info("📌 Détermination de l’Homme du Match | Nombre de joueurs en compétition : {}", statsRencontres.size());

        if (statsRencontres.isEmpty()) {
            log.warn("⚠️ Aucun joueur dans la liste pour HOM !");
            return null;
        }

        statsRencontres.forEach(joueur ->
                log.info("📊 Joueur={} | Points={}", joueur.getJoueurId(), joueur.getPoints()));

        Long hommeDuMatchId = statsRencontres.stream()
                .max(Comparator.comparingInt(StatistiquesRencontreDTO::getPoints))
                .map(StatistiquesRencontreDTO::getJoueurId)
                .orElse(null);

        if (hommeDuMatchId != null) {
            log.info("🏆 Homme du Match sélectionné : Joueur ID={}", hommeDuMatchId);
        } else {
            log.warn("⚠️ Aucun joueur n’a été sélectionné comme Homme du Match !");
        }

        return hommeDuMatchId;
    }













    private static List<Long> parsePasseurs(String passeursIds) {
        if (passeursIds != null && !passeursIds.isEmpty()) {
            return Arrays.stream(passeursIds.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private static List<String> parseNomsPasseurs(String nomsPasseurs) {
        if (nomsPasseurs != null && !nomsPasseurs.isEmpty()) {
            return Arrays.asList(nomsPasseurs.split(","));
        } else {
            return new ArrayList<>();
        }
    }














}
